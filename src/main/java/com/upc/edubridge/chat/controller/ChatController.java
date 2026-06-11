package com.upc.edubridge.chat.controller;

import com.upc.edubridge.chat.service.GeminiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Asistente AI", description = "Endpoints para la interacción con el chatbot inteligente de EduBridge")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @Operation(
            summary = "Consultar al asistente virtual",
            description = "Envía un mensaje al modelo de IA (Gemini) proporcionando contexto del usuario como nombre, rol y cursos matriculados para obtener una respuesta personalizada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta de la IA generada con éxito",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"answer\": \"Hola Carlos, tus cursos para este ciclo son...\"}")) }),
            @ApiResponse(responseCode = "400", description = "Error en el formato de la solicitud", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno al conectar con el servicio de IA", content = @Content)
    })
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAi(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String role = payload.get("role");
        String name = payload.get("userName");

        String cursos = payload.getOrDefault("cursos", "No especificados");

        String currentTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        String systemPrompt = String.format(
                "Eres el asistente oficial de EduBridge. Tu nombre es EduBridge AI. " +
                        "Usuario: %s | Rol: %s | Hora: %s. " +
                        "IMPORTANTE: El usuario está matriculado ÚNICAMENTE en estos cursos: [%s]. " +
                        "Si pregunta por sus cursos, usa solo esa lista. Si no te preguntan por ellos, no los menciones. " +
                        "Responde de forma concisa, humana y evita discursos motivacionales largos.",
                name, role, currentTime, cursos
        );

        String answer = geminiService.getAiResponse(systemPrompt, message);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}