package com.centromedico.profesionales;

import java.time.LocalTime;

import com.centromedico.especialidades.Especialidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Médico del centro. Su agenda se define con días de atención (LUN,MAR,MIE,JUE,VIE,SAB)
 * y una franja horaria; los turnos se generan en la grilla que marca su especialidad.
 */
@Entity
@Table(name = "profesionales")
public class Profesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La matrícula es obligatoria")
    @Size(max = 20, message = "Máximo 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String matricula;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener 7 u 8 dígitos, sin puntos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String apellido;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String nombre;

    @Pattern(regexp = "^$|^[0-9+()\\s-]{6,20}$", message = "Teléfono inválido")
    @Column(length = 20)
    private String telefono;

    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @NotBlank(message = "El consultorio es obligatorio")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String consultorio;

    @NotBlank(message = "Indique al menos un día de atención")
    @Pattern(regexp = "^(LUN|MAR|MIE|JUE|VIE|SAB)(,(LUN|MAR|MIE|JUE|VIE|SAB))*$",
            message = "Formato inválido. Ejemplo: LUN,MIE,VIE")
    @Column(nullable = false, length = 27)
    private String diasAtencion;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(nullable = false)
    private LocalTime horaDesde;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(nullable = false)
    private LocalTime horaHasta;

    @Column(nullable = false)
    private boolean activo = true;

    @NotNull(message = "La especialidad es obligatoria")
    @ManyToOne(optional = false)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    public Profesional() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getConsultorio() { return consultorio; }
    public void setConsultorio(String consultorio) { this.consultorio = consultorio; }

    public String getDiasAtencion() { return diasAtencion; }
    public void setDiasAtencion(String diasAtencion) { this.diasAtencion = diasAtencion; }

    public LocalTime getHoraDesde() { return horaDesde; }
    public void setHoraDesde(LocalTime horaDesde) { this.horaDesde = horaDesde; }

    public LocalTime getHoraHasta() { return horaHasta; }
    public void setHoraHasta(LocalTime horaHasta) { this.horaHasta = horaHasta; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
}
