package pe.cibertec.gym;

import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.cibertec.gym.model.GymClass;
import pe.cibertec.gym.model.Member;
import pe.cibertec.gym.repository.ClassEnrollmentRepository;
import pe.cibertec.gym.repository.GymClassRepository;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.service.EnrollmentService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EnrollmentTransactionTest {
    @Autowired private EnrollmentService service;
    @Autowired private ClassEnrollmentRepository enrollments;
    @Autowired private GymClassRepository classes;
    @Autowired private MemberRepository members;
    @Autowired private PlatformTransactionManager transactionManager;
    private TransactionTemplate transaction;
    private Fixture fixture;

    private record Fixture(Long classId, Long firstMemberId, Long secondMemberId) { }

    @BeforeEach
    void createIsolatedFixtures() {
        transaction = new TransactionTemplate(transactionManager);
        fixture = transaction.execute(status -> {
            var first = createMember("90100001");
            var second = createMember("90100002");
            var gymClass = new GymClass();
            gymClass.setNombre("Prueba transaccional");
            gymClass.setCupos(1);
            return new Fixture(classes.saveAndFlush(gymClass).getId(), first.getId(), second.getId());
        });
    }

    private Member createMember(String dni) {
        var member = new Member();
        member.setDni(dni);
        member.setNombres("Socio");
        member.setApellidos("Prueba");
        return members.saveAndFlush(member);
    }

    @AfterEach
    void removeOnlyFixturesFromThisTest() {
        if (fixture == null) return;
        transaction.executeWithoutResult(status -> {
            enrollments.deleteAll(enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(fixture.classId()));
            enrollments.flush();
            classes.deleteById(fixture.classId());
            members.deleteById(fixture.firstMemberId());
            members.deleteById(fixture.secondMemberId());
        });
    }

    @Test
    void enrollmentCommitsRowAndOccupiedCounterTogether() {
        service.enroll(fixture.classId(), fixture.firstMemberId());
        assertOccupation(1);
    }

    @Test
    void fullClassRejectionPreservesTheFirstEnrollment() {
        service.enroll(fixture.classId(), fixture.firstMemberId());
        assertThrows(IllegalStateException.class,
                () -> service.enroll(fixture.classId(), fixture.secondMemberId()));
        assertOccupation(1);
        assertFalse(enrollments.existsByGymClassIdAndMemberId(fixture.classId(), fixture.secondMemberId()));
    }

    @Test
    void cancellingTwiceDoesNotFreeAnExtraSlot() {
        service.enroll(fixture.classId(), fixture.firstMemberId());
        Long enrollmentId = enrollmentId();
        service.cancel(fixture.classId(), enrollmentId);
        service.cancel(fixture.classId(), enrollmentId);
        assertOccupation(0);
        assertEquals("CANCELADO", enrollments.findById(enrollmentId).orElseThrow().getEstado());
        service.enroll(fixture.classId(), fixture.secondMemberId());
        assertOccupation(1);
    }

    @Test
    void reactivationKeepsTheSameEnrollmentId() {
        service.enroll(fixture.classId(), fixture.firstMemberId());
        Long originalId = enrollmentId();
        service.cancel(fixture.classId(), originalId);
        service.enroll(fixture.classId(), fixture.firstMemberId());
        assertEquals(originalId, enrollmentId());
        assertEquals(1, enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(fixture.classId()).size());
        assertOccupation(1);
    }

    @Test
    void duplicateIsRejectedEvenWhenTheClassHasFreeCapacity() {
        transaction.executeWithoutResult(status -> {
            var gymClass = classes.findByIdForUpdate(fixture.classId()).orElseThrow();
            gymClass.setCupos(2);
            classes.save(gymClass);
        });
        service.enroll(fixture.classId(), fixture.firstMemberId());
        var error = assertThrows(IllegalStateException.class,
                () -> service.enroll(fixture.classId(), fixture.firstMemberId()));
        assertEquals("El miembro ya está inscrito en esta clase", error.getMessage());
        assertOccupation(1);
    }

    @Test
    void unknownMemberDoesNotConsumeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> service.enroll(fixture.classId(), Long.MAX_VALUE));
        assertOccupation(0);
    }

    @Test
    void runtimeFailureRollsBackEnrollmentAndCounter() {
        var error = assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            service.enroll(fixture.classId(), fixture.firstMemberId());
            throw new IllegalStateException("Fallo simulado dentro de la transacción");
        }));
        assertEquals("Fallo simulado dentro de la transacción", error.getMessage());
        assertOccupation(0);
        assertTrue(enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(fixture.classId()).isEmpty());
    }

    private Long enrollmentId() {
        return enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(fixture.classId()).getFirst().getId();
    }

    private void assertOccupation(int expected) {
        transaction.executeWithoutResult(status -> {
            var gymClass = classes.findById(fixture.classId()).orElseThrow();
            assertEquals(expected, Objects.requireNonNull(gymClass.getCuposOcupados()));
            assertEquals(expected, enrollments.countByGymClassIdAndEstado(fixture.classId(), "ACTIVO"));
        });
    }
}
