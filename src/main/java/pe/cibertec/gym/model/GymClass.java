package pe.cibertec.gym.model;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clase")
public class GymClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClase")
    private Long id;
    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank
    @Size(max=100)
    private String nombre;
    @Column(name = "descripcion")
    @Size(max=150)
    private String descripcion;
    @Column(name = "horario")
    @Size(max=100)
    private String horario;
    @Column(name = "cupos", nullable = false)
    @NotNull
    @Min(value=1, message="Debe existir al menos un cupo")
    private Integer cupos = 0;
    @Column(name = "cuposOcupados", nullable = false)
    private Integer cuposOcupados = 0;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEmpleado")
    private Employee employee;
    @Column(name = "estado", nullable = false, length = 20)
    @Pattern(regexp="ACTIVO|INACTIVO")
    private String estado = "ACTIVO";

    public GymClass() { }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public Integer getCupos() { return cupos; }
    public void setCupos(Integer cupos) { this.cupos = cupos; }
    public Integer getCuposOcupados() { return cuposOcupados; }
    public void setCuposOcupados(Integer cuposOcupados) { this.cuposOcupados = cuposOcupados; }
    public int getCuposDisponibles() { return Math.max(0, cupos - cuposOcupados); }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
