package com.upc.edubridge.chat.service;

import com.upc.edubridge.tutoring.model.TutoringSession;
import com.upc.edubridge.tutoring.repository.TutoringRepository;
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

    @Value("${google.gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private TutoringRepository tutoringRepository;

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
                String curso = parts[0];
                LocalDateTime fechaHora = LocalDateTime.parse(parts[1]);

                TutoringSession session = new TutoringSession();
                session.setCourseName(curso);
                session.setTeacherName("Prof. Asistente IA");
                session.setTopic("Consulta agendada vía Chat");
                session.setStartTime(fechaHora);
                session.setDurationMinutes(60);
                session.setStatus("Confirmada");
                session.setType("INDIVIDUAL");
                session.setStudentCount(1);

                tutoringRepository.save(session);

                return text.substring(0, text.indexOf("[DATA_TUTORING:")).trim()
                        + "\n\n✅ *Tutoría sincronizada con tu panel a las " + fechaHora.format(DateTimeFormatter.ofPattern("hh:mm a")) + ".*";

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