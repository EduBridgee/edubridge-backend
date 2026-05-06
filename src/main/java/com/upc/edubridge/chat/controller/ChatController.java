package com.upc.edubridge.chat.controller;

import com.upc.edubridge.chat.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

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