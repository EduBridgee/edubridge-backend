package com.upc.edubridge.chat.service;

import com.upc.edubridge.tutoring.model.TutoringSession;
import com.upc.edubridge.tutoring.repository.TutoringRepository;
import com.upc.edubridge.teacher.model.Teacher;
import com.upc.edubridge.teacher.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${nvidia.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private TutoringRepository tutoringRepository;

    @Autowired
    private TeacherRepository teacherRepository; 

    @Autowired
    private com.upc.edubridge.course.repository.CourseRepository courseRepository;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    public String getAiResponse(String systemPrompt, String userMessage) {
        String fechaHoy = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        String superPrompt = systemPrompt + "\n\n" +
                "CONTEXTO TEMPORAL: Hoy es " + fechaHoy + ".\n" +
                "INSTRUCCIÓN TÉCNICA: Si el usuario quiere agendar una tutoría, identifica el curso y la hora solicitada. " +
                "Al final de tu respuesta, añade exactamente esto: [DATA_TUTORING:Nombre del Curso|YYYY-MM-DDTHH:mm:ss]. " +
                "Calcula la fecha según lo que diga el usuario (ej: 'mañana a las 7pm').";

        String contextText = superPrompt + "\n\nUsuario: " + userMessage;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", contextText))))
        );

        try {
            String rawResponse = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/gemini-3-flash-preview:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.isError(), response ->
                            response.bodyToMono(String.class).flatMap(errorBody ->
                                    Mono.error(new RuntimeException("API Error: " + errorBody)))
                    )
                    .bodyToMono(Map.class)
                    .map(this::extractText)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            return procesarIntencion(rawResponse);

        } catch (Exception e) {
            return "Error con Gemini 3: " + e.getMessage();
        }
    }

    
    
    
    private String procesarIntencion(String text) {
        if (text == null) return "";

        if (text.contains("[DATA_TUTORING:")) {
            try {
                int start = text.indexOf("[DATA_TUTORING:") + 15;
                int end = text.indexOf("]", start);
                String dataRaw = text.substring(start, end);

                String[] parts = dataRaw.split("\\|");
                String cursoNombre = parts[0].trim();
                LocalDateTime fechaHora = LocalDateTime.parse(parts[1]);

                // Vincular profesor real según base de datos
                String nombreProfesorReal = "Por Asignar";
                try {
                    List<com.upc.edubridge.course.model.Course> cursos = courseRepository.findAll();
                    for (com.upc.edubridge.course.model.Course c : cursos) {
                        if (c.getName() != null && cursoNombre != null) {
                            String nameDb = c.getName().trim();
                            String nameRequested = cursoNombre.trim();
                            if (nameDb.equalsIgnoreCase(nameRequested) || 
                                nameDb.toLowerCase().contains(nameRequested.toLowerCase()) || 
                                nameRequested.toLowerCase().contains(nameDb.toLowerCase())) {
                                if (c.getTeacher() != null) {
                                    nombreProfesorReal = c.getTeacher().getName() != null ? c.getTeacher().getName().trim() : "Por Asignar";
                                }
                                cursoNombre = c.getName().trim();
                                break;
                            }
                        }
                    }
                    if (nombreProfesorReal.equals("Por Asignar")) {
                        List<Teacher> profesores = teacherRepository.findAll();
                        for (Teacher p : profesores) {
                            if (p.getCourse() != null && p.getCourse().getName() != null && cursoNombre != null) {
                                String nameDb = p.getCourse().getName().trim();
                                String nameRequested = cursoNombre.trim();
                                if (nameDb.equalsIgnoreCase(nameRequested) || 
                                    nameDb.toLowerCase().contains(nameRequested.toLowerCase()) || 
                                    nameRequested.toLowerCase().contains(nameDb.toLowerCase())) {
                                    nombreProfesorReal = p.getName() != null ? p.getName().trim() : "Por Asignar";
                                    cursoNombre = p.getCourse().getName().trim();
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Aviso: Falló la vinculación del docente real, usando fallback: " + e.getMessage());
                }

                
                TutoringSession session = new TutoringSession();
                session.setCourseName(cursoNombre);
                session.setTeacherName(nombreProfesorReal); 
                session.setTopic("Consulta agendada vía Chatbot AI");
                session.setStartTime(fechaHora);
                session.setDurationMinutes(60);

                
                session.setStatus("Pendiente");

                session.setType("INDIVIDUAL");
                session.setStudentCount(1);

                tutoringRepository.save(session);

                String textLimpio = text.substring(0, text.indexOf("[DATA_TUTORING:")).trim();
                return textLimpio + "\n\n✅ *Tutoría registrada como Pendiente con el Prof. " + nombreProfesorReal + " para las " + fechaHora.format(DateTimeFormatter.ofPattern("hh:mm a")) + ".*";

            } catch (Exception e) {
                System.err.println("Error procesando data de tutoría: " + e.getMessage());
            }
        }
        return text;
    }

    private String extractText(Map<?, ?> response) {
        try {
            if (response != null && response.containsKey("candidates")) {
                List<?> candidates = (List<?>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                    Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                    if (content != null && content.containsKey("parts")) {
                        List<?> parts = (List<?>) content.get("parts");
                        if (!parts.isEmpty()) {
                            return (String) ((Map<?, ?>) parts.get(0)).get("text");
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "Respuesta inesperada.";
    }
}