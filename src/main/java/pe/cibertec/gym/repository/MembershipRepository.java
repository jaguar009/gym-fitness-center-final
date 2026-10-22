package pe.cibertec.gym.repository;

import pe.cibertec.gym.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Membership m where m.id = :id")
    Optional<Membership> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = "member")
    List<Membership> findAllByOrderByFechaFinAsc();

    @Override
    @EntityGraph(attributePaths = "member")
    Optional<Membership> findById(Long id);
}
