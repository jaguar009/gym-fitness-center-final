package pe.cibertec.gym.repository;

import pe.cibertec.gym.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface MemberRepository extends JpaRepository<Member, Long> {
    long countByEstado(String estado);
    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select m from Member m
            where (:query is null or lower(concat(m.nombres, ' ', m.apellidos)) like lower(concat('%', :query, '%'))
                   or lower(m.dni) like lower(concat('%', :query, '%')))
              and (:status is null or m.estado = :status)
            order by m.apellidos, m.nombres
            """)
    List<Member> search(@Param("query") String query, @Param("status") String status);
}
