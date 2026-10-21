package pe.cibertec.gym.repository;

import pe.cibertec.gym.model.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {
    long countByEstado(String estado);
    @EntityGraph(attributePaths = "employee")
    List<GymClass> findAllByOrderByNombreAsc();

    @Override
    @EntityGraph(attributePaths = "employee")
    Optional<GymClass> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from GymClass c where c.id = :id")
    Optional<GymClass> findByIdForUpdate(@Param("id") Long id);
}
