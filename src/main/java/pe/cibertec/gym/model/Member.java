package pe.cibertec.gym.model;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "miembro")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMiembro")
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    @Pattern(regexp="[0-9]{8}", message="El DNI debe tener ocho dígitos")
    @NotBlank
    private String dni;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max=100)
    private String nombres;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max=100)
    private String apellidos;

    @Size(max=20)
    private String telefono;
    @Email(message="Ingrese un correo válido")
    @Size(max=100)
    private String correo;
    @Size(max=150)
    private String direccion;

    @Column(name = "fechaRegistro")
    private LocalDate registrationDate = LocalDate.now();

    @Column(nullable = false, length = 20)
    @Pattern(regexp="ACTIVO|INACTIVO")
    private String estado = "ACTIVO";

    public Member() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
