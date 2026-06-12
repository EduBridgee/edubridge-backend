package com.upc.edubridge.tutoring.service;

import com.upc.edubridge.tutoring.model.TutoringSession;
import com.upc.edubridge.tutoring.repository.TutoringRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TutoringService {

    @Autowired
    private TutoringRepository tutoringRepository;

    public List<TutoringSession> listarActivas() {
        return tutoringRepository.findAll();
    }

    public TutoringSession cancelar(Long id) {
        TutoringSession session = tutoringRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutoría no encontrada"));
        session.setStatus("Cancelada");
        return tutoringRepository.save(session);
    }

    public TutoringSession reprogramar(Long id, LocalDateTime nuevaFecha) {
        TutoringSession session = tutoringRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutoría no encontrada"));

        session.setStartTime(nuevaFecha);
        session.setStatus("Confirmada");

        return tutoringRepository.save(session);
    }

    public TutoringSession crear(TutoringSession session) {
        session.setStatus("Confirmada");
        if (session.getStudentCount() == null) session.setStudentCount(1);
        return tutoringRepository.save(session);
    }
}