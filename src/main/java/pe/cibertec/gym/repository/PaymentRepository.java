package pe.cibertec.gym.repository;

import pe.cibertec.gym.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("select coalesce(sum(p.monto), 0) from Payment p")
    BigDecimal totalAmount();

    @EntityGraph(attributePaths = "member")
    @Query("""
            select p from Payment p
            where p.fechaPago between :from and :to
              and (:method is null or p.metodoPago = :method)
            order by p.fechaPago desc, p.id desc
            """)
    List<Payment> search(@Param("from") LocalDate from, @Param("to") LocalDate to,
                         @Param("method") String method);

    @Override
    @EntityGraph(attributePaths = "member")
    Optional<Payment> findById(Long id);
}
