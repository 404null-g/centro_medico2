package com.centromedico.pacientes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    /** Búsqueda por apellido, nombre o inicio del DNI (lo que usa recepción al atender el teléfono). */
    @Query("""
            select p from Paciente p
            where lower(p.apellido) like lower(concat('%', :q, '%'))
               or lower(p.nombre) like lower(concat('%', :q, '%'))
               or p.dni like concat(:q, '%')
            order by p.apellido, p.nombre
            """)
    List<Paciente> buscar(@Param("q") String q);

    List<Paciente> findAllByOrderByApellidoAscNombreAsc();
}
