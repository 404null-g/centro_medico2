package com.centromedico.pacientes;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Persona que se atiende en el centro médico.
 * Si obraSocial es null o vacío, el paciente es "particular".
 * La baja es lógica (activo = false) para no perder la historia clínica.
 */
@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener 7 u 8 dígitos, sin puntos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    @Column(nullable = false, length = 60)
    private String apellido;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    @Column(nullable = false, length = 60)
    private String nombre;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El sexo es obligatorio")
    @Pattern(regexp = "[FMX]", message = "Valores permitidos: F, M, X")
    @Column(nullable = false, length = 1)
    private String sexo;

    @Pattern(regexp = "^$|^[0-9+()\\s-]{6,20}$", message = "Teléfono inválido")
    @Column(length = 20)
    private String telefono;

    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Size(max = 120)
    @Column(length = 120)
    private String domicilio;

    @Size(max = 60)
    @Column(length = 60)
    private String localidad;

    @Size(max = 60)
    @Column(length = 60)
    private String obraSocial;

    @Size(max = 30)
    @Column(length = 30)
    private String nroAfiliado;

    @Column(nullable = false)
    private boolean activo = true;

    public Paciente() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getObraSocial() { return obraSocial; }
    public void setObraSocial(String obraSocial) { this.obraSocial = obraSocial; }

    public String getNroAfiliado() { return nroAfiliado; }
    public void setNroAfiliado(String nroAfiliado) { this.nroAfiliado = nroAfiliado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
