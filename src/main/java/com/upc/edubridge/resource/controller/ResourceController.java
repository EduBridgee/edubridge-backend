package com.upc.edubridge.resource.controller;

import com.upc.edubridge.resource.model.Resource;
import com.upc.edubridge.resource.repository.ResourceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Recursos Académicos", description = "Endpoints para la gestión de materiales de estudio y recursos compartidos")
public class ResourceController {

    @Autowired
    private ResourceRepository resourceRepository;

    @Operation(
            summary = "Obtener todos los recursos",
            description = "Retorna una lista de todos los materiales académicos disponibles (PDFs, enlaces, documentos) registrados en la plataforma."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de recursos obtenida con éxito",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Resource.class))
                            )
                    }
            ),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @GetMapping
    public List<Resource> getAll() {
        return resourceRepository.findAll();
    }

    @Operation(
            summary = "Crear nuevo recurso",
            description = "Registra un nuevo material de estudio en la base de datos de EduBridge."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurso creado exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Resource.class)) }),
            @ApiResponse(responseCode = "400", description = "Datos del recurso inválidos", content = @Content)
    })
    @PostMapping
    public Resource createResource(@RequestBody Resource resource) {
        return resourceRepository.save(resource);
    }
}