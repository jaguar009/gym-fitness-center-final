package pe.cibertec.gym.model;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "empleado")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEmpleado")
    private Long id;
    @Column(name = "dni", nullable = false, unique = true, length = 15)
    @Pattern(regexp="[0-9]{8}", message="El DNI debe tener ocho dígitos")
    @NotBlank
    private String dni;
    @Column(name = "nombres", nullable = false, length = 100)
    @NotBlank
    @Size(max=100)
    private String nombres;
    @Column(name = "apellidos", nullable = false, length = 100)
    @NotBlank
    @Size(max=100)
    private String apellidos;
    @Size(max=50)
    private String cargo;
    @Size(max=20)
    private String telefono;
    @Email
    @Size(max=100)
    private String correo;
    @Column(name = "estado", nullable = false, length = 20)
    @Pattern(regexp="ACTIVO|INACTIVO")
    private String estado = "ACTIVO";

    public Employee() { }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
