package com.centromedico.atenciones;

import java.time.LocalDateTime;

import com.centromedico.turnos.Turno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registro clínico de una consulta. Cada atención nace de un turno (uno a uno);
 * el conjunto de atenciones de un paciente es su historia clínica.
 */
@Entity
@Table(name = "atenciones")
public class Atencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Indique el turno que se atendió")
    @OneToOne(optional = false)
    @JoinColumn(name = "turno_id", nullable = false, unique = true)
    private Turno turno;

    @NotBlank(message = "El motivo de consulta es obligatorio")
    @Size(max = 300, message = "Máximo 300 caracteres")
    @Column(nullable = false, length = 300)
    private String motivoConsulta;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(max = 500, message = "Máximo 500 caracteres")
    @Column(nullable = false, length = 500)
    private String diagnostico;

    @Size(max = 1000, message = "Máximo 1000 caracteres")
    @Column(length = 1000)
    private String indicaciones;

    @Size(max = 4000, message = "Máximo 4000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, updatable = false, length = 30)
    private String registradaPor;

    public Atencion() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getRegistradaPor() { return registradaPor; }
    public void setRegistradaPor(String registradaPor) { this.registradaPor = registradaPor; }
}
