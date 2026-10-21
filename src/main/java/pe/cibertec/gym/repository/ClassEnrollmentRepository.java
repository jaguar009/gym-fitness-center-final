package pe.cibertec.gym.repository;

import pe.cibertec.gym.model.ClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {
    boolean existsByGymClassIdAndMemberId(Long classId, Long memberId);
    long countByGymClassIdAndEstado(Long classId, String estado);
    @EntityGraph(attributePaths = "member")
    List<ClassEnrollment> findByGymClassIdOrderByFechaInscripcionDescIdDesc(Long classId);
}
