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
@Table(name = "pago")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPago")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idMiembro", nullable = false)
    private Member member;
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value="0.01", message="El monto debe ser mayor que cero")
    @Digits(integer=8, fraction=2)
    private BigDecimal monto;
    @Column(name = "fechaPago")
    @NotNull
    private LocalDate fechaPago = LocalDate.now();
    @Column(name = "metodoPago")
    @NotNull
    @Pattern(regexp="EFECTIVO|YAPE|TARJETA|TRANSFERENCIA")
    private String metodoPago;
    @Column(name = "concepto")
    @Size(max=100)
    private String concepto;

    public Payment() { }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
}
