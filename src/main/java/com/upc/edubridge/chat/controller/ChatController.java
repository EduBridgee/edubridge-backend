package com.upc.edubridge.chat.controller;

import com.upc.edubridge.chat.service.GeminiService;
import com.upc.edubridge.enrollment.model.Enrollment;
import com.upc.edubridge.enrollment.repository.EnrollmentRepository;
import com.upc.edubridge.grade.model.Grade;
import com.upc.edubridge.grade.repository.GradeRepository;
import com.upc.edubridge.tutoring.model.TutoringSession;
import com.upc.edubridge.tutoring.repository.TutoringRepository;
import com.upc.edubridge.teacher.model.Teacher;
import com.upc.edubridge.teacher.model.TeacherTask;
import com.upc.edubridge.teacher.repository.TeacherRepository;
import com.upc.edubridge.teacher.repository.TeacherTaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Asistente AI", description = "Endpoints para la interacción con el chatbot inteligente de EduBridge")
@RequiredArgsConstructor 
public class ChatController {

    private final GeminiService geminiService;
    private final EnrollmentRepository enrollmentRepository; 
    private final GradeRepository gradeRepository;
    private final TutoringRepository tutoringRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherTaskRepository teacherTaskRepository; 

    @Operation(
            summary = "Consultar al asistente virtual",
            description = "Envía un mensaje al modelo de IA cruzando la información real de la base de datos para evitar alucinaciones."
    )
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAi(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String role = payload.get("role");
        String name = payload.get("userName");
        String userIdStr = payload.get("userId");

        String infoNotas = "No aplica.";
        String infoTutorias = "No hay tutorías programadas.";
        String infoTareasDocente = "No aplica.";
        String infoEstudiantesDocente = "No aplica.";
        String listaCursosValidada = "No especificados";

        if (userIdStr != null) {
            try {
                Long parsedUserId = Long.valueOf(userIdStr);
                
                if (role != null && (role.equalsIgnoreCase("ESTUDIANTE") || role.equalsIgnoreCase("estudiante"))) {
                    List<Enrollment> matriculasActivas = enrollmentRepository.findByStudentId(parsedUserId);
                    if (!matriculasActivas.isEmpty()) {
                        listaCursosValidada = matriculasActivas.stream()
                                .map(e -> e.getCourse().getName()) 
                                .collect(Collectors.joining(", "));
                    }

                    List<Grade> notas = gradeRepository.findByStudentId(parsedUserId);
                    if (notas != null && !notas.isEmpty()) {
                        infoNotas = notas.stream()
                                .map(g -> String.format("- %s (%s): %.1f", 
                                        g.getCourse() != null ? g.getCourse().getName() : "Curso", 
                                        g.getType() != null ? g.getType().toString() : "Evaluación", 
                                        g.getValue() != null ? g.getValue() : 0.0))
                                .collect(Collectors.joining("\n"));
                    }

                    if (!matriculasActivas.isEmpty()) {
                        final List<String> nombresCursos = matriculasActivas.stream()
                                .map(e -> e.getCourse().getName())
                                .collect(Collectors.toList());

                        List<TutoringSession> todasTutorias = tutoringRepository.findAll();
                        List<TutoringSession> tutoriasUsuario = todasTutorias.stream()
                                .filter(t -> nombresCursos.contains(t.getCourseName()))
                                .collect(Collectors.toList());

                        if (!tutoriasUsuario.isEmpty()) {
                            infoTutorias = tutoriasUsuario.stream()
                                    .map(t -> String.format("- [ID: %d] %s con Prof. %s el %s (Tema: %s, Estado: %s)", 
                                            t.getId(),
                                            t.getCourseName(), 
                                            t.getTeacherName(), 
                                            t.getStartTime().toString(), 
                                            t.getTopic(), 
                                            t.getStatus()))
                                    .collect(Collectors.joining("\n"));
                        }
                                  } else if (role != null && (role.equalsIgnoreCase("DOCENTE") || role.equalsIgnoreCase("docente"))) {
                    java.util.Optional<Teacher> teacherOpt = teacherRepository.findById(parsedUserId);
                    if (teacherOpt.isPresent()) {
                        Teacher doc = teacherOpt.get();
                        if (doc.getCourses() != null && !doc.getCourses().isEmpty()) {
                            listaCursosValidada = doc.getCourses().stream()
                                    .map(c -> c.getName())
                                    .collect(Collectors.joining(", "));
                            
                            List<Long> courseIds = doc.getCourses().stream()
                                    .map(c -> c.getId())
                                    .collect(Collectors.toList());
                            
                            List<Enrollment> matriculasCursos = enrollmentRepository.findAll().stream()
                                    .filter(e -> e.getCourse() != null && courseIds.contains(e.getCourse().getId()))
                                    .collect(Collectors.toList());
                            
                            if (!matriculasCursos.isEmpty()) {
                                infoEstudiantesDocente = matriculasCursos.stream()
                                        .map(e -> String.format("- Curso: %s | Alumno: %s (Código: %s, Email: %s)", 
                                                e.getCourse().getName(),
                                                e.getStudent() != null ? e.getStudent().getName() : "Desconocido",
                                                e.getStudent() != null ? e.getStudent().getCode() : "N/A",
                                                e.getStudent() != null ? e.getStudent().getEmail() : "N/A"))
                                        .collect(Collectors.joining("\n"));
                            } else {
                                infoEstudiantesDocente = "No hay alumnos matriculados en tus cursos actualmente.";
                            }

                            List<Grade> notasAlumnosDocente = gradeRepository.findAll().stream()
                                    .filter(g -> g.getCourse() != null && courseIds.contains(g.getCourse().getId()))
                                    .collect(Collectors.toList());
                            
                            if (!notasAlumnosDocente.isEmpty()) {
                                infoNotas = notasAlumnosDocente.stream()
                                        .map(g -> String.format("- Alumno: %s | Curso: %s | Evaluacion: %s | Nota: %.1f", 
                                                g.getStudent() != null ? g.getStudent().getName() : "Desconocido",
                                                g.getCourse() != null ? g.getCourse().getName() : "Curso",
                                                g.getType() != null ? g.getType().toString() : "Evaluación",
                                                g.getValue() != null ? g.getValue() : 0.0))
                                        .collect(Collectors.joining("\n"));
                            } else {
                                infoNotas = "No hay notas registradas para tus cursos actualmente.";
                            }
                            System.out.println("DEBUG CHATBOT FOR TEACHER: " + doc.getName());
                            System.out.println("DEBUG courseIds: " + courseIds);
                            System.out.println("DEBUG matriculasCursos size: " + matriculasCursos.size());
                            System.out.println("DEBUG notasAlumnosDocente size: " + notasAlumnosDocente.size());
                        }
                        
                        String docName = doc.getName();
                        List<TutoringSession> todasTutorias = tutoringRepository.findAll();
                        List<TutoringSession> tutoriasDoc = todasTutorias.stream()
                                .filter(t -> t.getTeacherName() != null && t.getTeacherName().equalsIgnoreCase(docName))
                                .collect(Collectors.toList());
                                
                        if (!tutoriasDoc.isEmpty()) {
                            infoTutorias = tutoriasDoc.stream()
                                    .map(t -> String.format("- [ID: %d] %s con estudiante el %s (Tema: %s, Estado: %s)", 
                                            t.getId(),
                                            t.getCourseName(), 
                                            t.getStartTime().toString(), 
                                            t.getTopic(), 
                                            t.getStatus()))
                                    .collect(Collectors.joining("\n"));
                        }
                    }
                    
                    List<TeacherTask> tareas = teacherTaskRepository.findAll();
                    if (!tareas.isEmpty()) {
                        infoTareasDocente = tareas.stream()
                                .map(t -> String.format("- [ID: %d] %s (Curso/Tag: %s, Progreso: %d%%, Estado: %s, Vence: %s)", 
                                        t.getId(),
                                        t.getTitle(),
                                        t.getTag(),
                                        t.getProgress(),
                                        t.getStatus(),
                                        t.getDueDate()))
                                .collect(Collectors.joining("\n"));
                    }        }
                }
            } catch (Exception e) {
                System.err.println("Error al recuperar contexto para el asistente AI: " + e.getMessage());
            }
        }

        String currentTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Lima"))
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        String systemPrompt = String.format(
                "Eres el asistente oficial de la plataforma universitaria EduBridge. Tu nombre es EduBridge AI.\n" +
                "DATOS DE USUARIO: Nombre: %s | Rol: %s | Hora actual: %s.\n" +
                "CURSOS RELACIONADOS: [%s].\n\n" +
                "CALIFICACIONES DE ESTUDIANTES:\n%s\n\n" +
                "TUTORÍAS PROGRAMADAS:\n%s\n\n" +
                "TAREAS/RECORDATORIOS DOCENTE:\n%s\n\n" +
                "ALUMNOS MATRICULADOS EN TUS CURSOS (Solo docente/admin):\n%s\n\n" +
                "INSTRUCCIÓN GENERAL:\n" +
                "- Responde de manera amigable, concisa y humana.\n" +
                "- Responde las preguntas sobre notas, promedios, tutorías y listas de alumnos matriculados basándote estrictamente en los datos reales proveídos arriba. Si no hay datos, indícalo educadamente.\n" +
                "- Para agendar una tutoría, usa el formato [DATA_TUTORING:Nombre del Curso|YYYY-MM-DDTHH:mm:ss] al final de tu respuesta.\n" +
                "- Si un docente te pide crear un recordatorio o tarea, responde con normalidad y al final añade exactamente esto: [CREATE_TEACHER_TASK:Título de la tarea|Curso o Tag|YYYY-MM-DD].\n" +
                "- Si te piden cancelar una tutoría, identifica su ID de la lista de tutorías de arriba y añade exactamente esto al final de tu respuesta: [CANCEL_TUTORING:ID].\n" +
                "- Si te piden recursos, PDFs o material de estudio de un curso, añade exactamente esto al final: [RECOMMEND_RESOURCE:Nombre del Curso]. Nunca uses esta etiqueta de recomendación de recursos si el Rol del usuario conectado es DOCENTE o TEACHER.",
                name, role, currentTime, listaCursosValidada, infoNotas, infoTutorias, infoTareasDocente, infoEstudiantesDocente
        );

        String answer = geminiService.getAiResponse(systemPrompt, message);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}