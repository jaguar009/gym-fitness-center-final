package pe.cibertec.gym.repository;

import pe.cibertec.gym.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @EntityGraph(attributePaths = "member")
    List<Attendance> findAllByOrderByFechaDescIdDesc();

    @Override
    @EntityGraph(attributePaths = "member")
    Optional<Attendance> findById(Long id);
}
