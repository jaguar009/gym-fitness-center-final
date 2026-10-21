package pe.cibertec.gym.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(name = "inscripcion_clase", uniqueConstraints = @UniqueConstraint(name = "uk_clase_miembro", columnNames = {"idClase", "idMiembro"}))
public class ClassEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscripcion")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idClase", nullable = false)
    private GymClass gymClass;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idMiembro", nullable = false)
    private Member member;
    @Column(name = "fechaInscripcion", nullable = false)
    private LocalDate fechaInscripcion = LocalDate.now();
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    public ClassEnrollment() { }
    public ClassEnrollment(GymClass gymClass, Member member) {
        this.gymClass = gymClass;
        this.member = member;
    }
    public Long getId() { return id; }
    public GymClass getGymClass() { return gymClass; }
    public Member getMember() { return member; }
    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
