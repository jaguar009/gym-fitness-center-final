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
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "membresia")
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMembresia")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idMiembro", nullable = false)
    private Member member;
    @Column(name = "tipo", nullable = false, length = 50)
    @NotBlank
    @Size(max=50)
    private String tipo;
    @Column(name = "costo", nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value="0.01", message="El costo debe ser mayor que cero")
    @Digits(integer=8, fraction=2)
    private BigDecimal costo;
    @Column(name = "fechaInicio")
    @NotNull
    private LocalDate fechaInicio;
    @Column(name = "fechaFin")
    @NotNull
    private LocalDate fechaFin;
    @Column(name = "estado", nullable = false, length = 20)
    @Pattern(regexp="ACTIVO|INACTIVO|VENCIDO")
    private String estado = "ACTIVO";

    public Membership() { }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
