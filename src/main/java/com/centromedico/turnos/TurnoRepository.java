package com.centromedico.turnos;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByProfesionalIdAndFechaAndEstadoIn(Long profesionalId, LocalDate fecha,
            Collection<EstadoTurno> estados);

    List<Turno> findByPacienteIdAndFechaAndEstadoIn(Long pacienteId, LocalDate fecha,
            Collection<EstadoTurno> estados);

    List<Turno> findByProfesionalIdAndFechaOrderByHoraAsc(Long profesionalId, LocalDate fecha);

    @Query("""
            select t from Turno t
            where (:fecha is null or t.fecha = :fecha)
              and (:estado is null or t.estado = :estado)
              and (:pacienteId is null or t.paciente.id = :pacienteId)
              and (:profesionalId is null or t.profesional.id = :profesionalId)
            order by t.fecha, t.hora
            """)
    List<Turno> filtrar(@Param("fecha") LocalDate fecha,
            @Param("estado") EstadoTurno estado,
            @Param("pacienteId") Long pacienteId,
            @Param("profesionalId") Long profesionalId);
}
