package com.centromedico.profesionales;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByEspecialidadIdAndActivoTrue(Long especialidadId);

    @Query("""
            select p from Profesional p
            where (:q is null or lower(p.apellido) like lower(concat('%', :q, '%'))
                              or lower(p.nombre) like lower(concat('%', :q, '%')))
              and (:especialidadId is null or p.especialidad.id = :especialidadId)
              and (:soloActivos = false or p.activo = true)
            order by p.apellido, p.nombre
            """)
    List<Profesional> filtrar(@Param("q") String q,
            @Param("especialidadId") Long especialidadId,
            @Param("soloActivos") boolean soloActivos);
}
