package com.centromedico.especialidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Especialidad médica. Define cuánto dura un turno: una consulta de Cardiología (30 min)
 * no dura lo mismo que una de Clínica Médica (20 min). Esa duración arma la grilla de turnos.
 */
@Entity
@Table(name = "especialidades")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Size(max = 200, message = "Máximo 200 caracteres")
    @Column(length = 200)
    private String descripcion;

    @NotNull(message = "La duración del turno es obligatoria")
    @Min(value = 10, message = "La duración mínima es de 10 minutos")
    @Max(value = 120, message = "La duración máxima es de 120 minutos")
    @Column(nullable = false)
    private Integer duracionTurnoMin;

    @Column(nullable = false)
    private boolean activa = true;

    public Especialidad() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getDuracionTurnoMin() { return duracionTurnoMin; }
    public void setDuracionTurnoMin(Integer duracionTurnoMin) { this.duracionTurnoMin = duracionTurnoMin; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
