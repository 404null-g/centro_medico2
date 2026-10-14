package com.centromedico.especialidades;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Especialidad> findAllByOrderByNombreAsc();

    List<Especialidad> findByActivaTrueOrderByNombreAsc();
}
