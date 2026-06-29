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
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private TutoringRepository tutoringRepository;

    @Autowired
    private TeacherRepository teacherRepository; 

    @Autowired
    private com.upc.edubridge.course.repository.CourseRepository courseRepository;

    @Autowired
    private com.upc.edubridge.teacher.repository.TeacherTaskRepository teacherTaskRepository;

    @Autowired
    private com.upc.edubridge.resource.repository.ResourceRepository resourceRepository;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.groq.com/openai").build();
    }

    public String getAiResponse(String systemPrompt, String userMessage) {
        String fechaHoy = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Lima"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        String superPrompt = systemPrompt + "\n\n" +
                "CONTEXTO TEMPORAL: Hoy es " + fechaHoy + ".\n" +
                "INSTRUCCIÓN TÉCNICA: Si el usuario quiere agendar una tutoría, identifica el curso y la hora solicitada. " +
                "Al final de tu respuesta, añade exactamente esto: [DATA_TUTORING:Nombre del Curso|YYYY-MM-DDTHH:mm:ss]. " +
                "Calcula la fecha según lo que diga el usuario (ej: 'mañana a las 7pm').";

        Map<String, Object> body = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of("role", "system", "content", superPrompt),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        try {
            String rawResponse = webClient.post()
                    .uri("/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.isError(), response ->
                            response.bodyToMono(String.class).flatMap(errorBody ->
                                    Mono.error(new RuntimeException("API Error: " + errorBody)))
                    )
                    .bodyToMono(Map.class)
                    .map(this::extractText)
                    .timeout(Duration.ofSeconds(45))
                    .block();

            return procesarIntencion(rawResponse);

        } catch (Exception e) {
            return "Error con Groq (Llama): " + e.getMessage();
        }
    }

    
    
    
    private String procesarIntencion(String text) {
        if (text == null) return "";

        // 1. INTENCIÓN: AGENDAR TUTORÍA
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

        // 2. INTENCIÓN: CREAR TAREA DEL DOCENTE
        if (text.contains("[CREATE_TEACHER_TASK:")) {
            try {
                int start = text.indexOf("[CREATE_TEACHER_TASK:") + 21;
                int end = text.indexOf("]", start);
                String dataRaw = text.substring(start, end);

                String[] parts = dataRaw.split("\\|");
                String titulo = parts[0].trim();
                String tag = parts[1].trim();
                String fechaVence = parts[2].trim();

                com.upc.edubridge.teacher.model.TeacherTask task = new com.upc.edubridge.teacher.model.TeacherTask();
                task.setTitle(titulo);
                task.setTag(tag);
                task.setStatus("pendiente");
                task.setProgress(0);
                task.setDueDate(fechaVence);

                teacherTaskRepository.save(task);

                String textLimpio = text.substring(0, text.indexOf("[CREATE_TEACHER_TASK:")).trim();
                return textLimpio + "\n\n✅ *Recordatorio creado: '" + titulo + "' (" + tag + ") vence el " + fechaVence + ".*";
            } catch (Exception e) {
                System.err.println("Error creando tarea de docente: " + e.getMessage());
            }
        }

        // 3. INTENCIÓN: CANCELAR TUTORÍA
        if (text.contains("[CANCEL_TUTORING:")) {
            try {
                int start = text.indexOf("[CANCEL_TUTORING:") + 17;
                int end = text.indexOf("]", start);
                String idRaw = text.substring(start, end).trim();

                Long sessionId = Long.valueOf(idRaw);
                java.util.Optional<TutoringSession> sessionOpt = tutoringRepository.findById(sessionId);
                if (sessionOpt.isPresent()) {
                    TutoringSession session = sessionOpt.get();
                    session.setStatus("Cancelada");
                    tutoringRepository.save(session);

                    String textLimpio = text.substring(0, text.indexOf("[CANCEL_TUTORING:")).trim();
                    return textLimpio + "\n\n❌ *Tutoría [ID: " + sessionId + "] de " + session.getCourseName() + " ha sido cancelada.*";
                } else {
                    String textLimpio = text.substring(0, text.indexOf("[CANCEL_TUTORING:")).trim();
                    return textLimpio + "\n\n⚠️ *No se encontró ninguna tutoría con el ID " + sessionId + ".*";
                }
            } catch (Exception e) {
                System.err.println("Error cancelando tutoría: " + e.getMessage());
            }
        }

        // 4. INTENCIÓN: RECOMENDAR RECURSOS
        if (text.contains("[RECOMMEND_RESOURCE:")) {
            try {
                int start = text.indexOf("[RECOMMEND_RESOURCE:") + 20;
                int end = text.indexOf("]", start);
                String cursoNombre = text.substring(start, end).trim();

                List<com.upc.edubridge.resource.model.Resource> todosRecursos = resourceRepository.findAll();
                List<com.upc.edubridge.resource.model.Resource> recursosFiltrados = todosRecursos.stream()
                        .filter(r -> sonCursosSimilares(r.getSubject(), cursoNombre))
                        .collect(Collectors.toList());

                String textLimpio = text.substring(0, text.indexOf("[RECOMMEND_RESOURCE:")).trim();
                
                if (!recursosFiltrados.isEmpty()) {
                    String listaRecursosStr = recursosFiltrados.stream()
                            .map(r -> String.format("📂 *%s* (%s) - Rating: %.1f★\n   [Ver Recurso](%s)", 
                                    r.getTitle(), r.getType(), r.getRating(), obtenerUrlRecurso(r)))
                            .collect(Collectors.joining("\n\n"));
                    return textLimpio + "\n\nAquí tienes material de estudio recomendado para *" + cursoNombre + "*:\n\n" + listaRecursosStr;
                } else {
                    return textLimpio + "\n\n⚠️ *No se encontraron archivos o recursos disponibles en la biblioteca para " + cursoNombre + " actualmente.*";
                }
            } catch (Exception e) {
                System.err.println("Error buscando recursos recomendados: " + e.getMessage());
            }
        }
        return text;
    }

    private String obtenerUrlRecurso(com.upc.edubridge.resource.model.Resource r) {
        String img = r.getImg();
        if (img != null && img.startsWith("data:")) {
            try {
                org.springframework.web.context.request.ServletRequestAttributes attributes = 
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
                    String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
                    return baseUrl + "/api/resources/image/" + r.getId();
                }
            } catch (Exception e) {
                System.err.println("Error al construir URL dinámica de imagen: " + e.getMessage());
            }
        }
        return img != null ? img : "";
    }

    private boolean sonCursosSimilares(String c1, String c2) {
        if (c1 == null || c2 == null) return false;
        String n1 = normalizarTexto(c1);
        String n2 = normalizarTexto(c2);
        if (n1.contains(n2) || n2.contains(n1)) return true;
        
        String[] palabras1 = c1.toLowerCase().split("\\s+");
        String[] palabras2 = c2.toLowerCase().split("\\s+");
        for (String p1 : palabras1) {
            String p1Norm = normalizarTexto(p1);
            if (p1Norm.length() < 4 || p1Norm.equals("para") || p1Norm.equals("como")) continue;
            for (String p2 : palabras2) {
                String p2Norm = normalizarTexto(p2);
                if (p2Norm.length() < 4) continue;
                if (p1Norm.contains(p2Norm) || p2Norm.contains(p1Norm) || 
                    (p1Norm.startsWith("ingen") && p2Norm.startsWith("ingen"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return texto.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9]", "");
    }

    private String extractText(Map<?, ?> response) {
        try {
            if (response != null && response.containsKey("choices")) {
                List<?> choices = (List<?>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                    if (message != null && message.containsKey("content")) {
                        return (String) message.get("content");
                    }
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return "Respuesta inesperada de Groq (Llama).";
    }
}