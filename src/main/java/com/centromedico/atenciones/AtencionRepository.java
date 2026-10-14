package com.centromedico.atenciones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    boolean existsByTurnoId(Long turnoId);

    /** Historia clínica de un paciente, de la más reciente a la más antigua. */
    List<Atencion> findByTurnoPacienteIdOrderByFechaHoraDesc(Long pacienteId);

    List<Atencion> findByTurnoProfesionalIdOrderByFechaHoraDesc(Long profesionalId);
}
