package pe.cibertec.gym;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
@ActiveProfiles("sqlserver-it")
class SqlServerConcurrencyIT {
    @Autowired private EnrollmentService service;
    @Autowired private ClassEnrollmentRepository enrollments;
    @Autowired private GymClassRepository classes;
    @Autowired private MemberRepository members;
    @Autowired private PlatformTransactionManager transactionManager;
    private TransactionTemplate transaction;
    private Long classId;
    private Long firstId;
    private Long secondId;

    @DynamicPropertySource
    static void isolatedDatabaseOnly(DynamicPropertyRegistry properties) {
        String url = System.getenv("GYM_SQL_IT_URL");
        String normalized = url == null ? "" : url.toLowerCase(Locale.ROOT);
        List<String> databaseSettings = Arrays.stream(normalized.split(";"))
                .filter(part -> part.startsWith("databasename=")).toList();
        if (!normalized.startsWith("jdbc:sqlserver://localhost;")
                || databaseSettings.size() != 1
                || !databaseSettings.getFirst().matches("databasename=gymfitnessdb_it_[a-z0-9_]+")
                || normalized.contains("failoverpartner=")
                || !normalized.contains(";integratedsecurity=true;")) {
            throw new IllegalStateException("Use GYM_SQL_IT_URL con una base local exclusiva gymfitnessdb_it_* y autenticación de Windows");
        }
        properties.add("spring.datasource.url", () -> url);
    }

    @BeforeEach
    void createFixtures() {
        transaction = new TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            firstId = member("93000001");
            secondId = member("93000002");
            var item = new GymClass();
            item.setNombre("Prueba SQL " + UUID.randomUUID());
            item.setCupos(1);
            classId = classes.saveAndFlush(item).getId();
        });
    }

    private Long member(String dni) {
        var item = new Member();
        item.setDni(dni);
        item.setNombres("Prueba aislada");
        item.setApellidos("SQL Server");
        return members.saveAndFlush(item).getId();
    }

    @AfterEach
    void removeFixtures() {
        if (classId == null) return;
        transaction.executeWithoutResult(status -> {
            enrollments.deleteAll(enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(classId));
            enrollments.flush();
            classes.deleteById(classId);
            members.deleteById(firstId);
            members.deleteById(secondId);
        });
    }

    @Test
    void onlyOneRequestCanTakeTheLastSlot() throws Exception {
        List<String> results = runTogether(() -> enroll(firstId), () -> enroll(secondId));
        assertEquals(1, results.stream().filter("ACEPTADO"::equals).count());
        assertEquals(1, results.stream().filter("La clase no tiene cupos disponibles"::equals).count());
        assertOccupation(1);
        assertEquals(1, enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(classId).size());
    }

    @Test
    void simultaneousDuplicateDoesNotCreateTwoRows() throws Exception {
        transaction.executeWithoutResult(status -> {
            var item = classes.findByIdForUpdate(classId).orElseThrow();
            item.setCupos(2);
            classes.save(item);
        });
        List<String> results = runTogether(() -> enroll(firstId), () -> enroll(firstId));
        assertEquals(1, results.stream().filter("ACEPTADO"::equals).count());
        assertEquals(1, results.stream().filter("El miembro ya está inscrito en esta clase"::equals).count());
        assertOccupation(1);
        assertEquals(1, enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(classId).size());
    }

    @Test
    void simultaneousCancellationDoesNotProduceNegativeOccupation() throws Exception {
        service.enroll(classId, firstId);
        Long enrollmentId = enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(classId).getFirst().getId();
        List<String> results = runTogether(() -> cancel(enrollmentId), () -> cancel(enrollmentId));
        assertEquals(List.of("CANCELADO", "CANCELADO"), results);
        assertOccupation(0);
        assertEquals("CANCELADO", enrollments.findById(enrollmentId).orElseThrow().getEstado());
    }

    @Test
    void runtimeFailureRollsBackBothWritesInSqlServer() {
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            service.enroll(classId, firstId);
            throw new IllegalStateException("Fallo simulado en SQL Server");
        }));
        assertOccupation(0);
        assertTrue(enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(classId).isEmpty());
    }

    private String enroll(Long memberId) {
        try {
            service.enroll(classId, memberId);
            return "ACEPTADO";
        } catch (IllegalStateException expectedBusinessRejection) {
            return expectedBusinessRejection.getMessage();
        }
    }

    private String cancel(Long enrollmentId) {
        service.cancel(classId, enrollmentId);
        return "CANCELADO";
    }

    private List<String> runTogether(Callable<String> first, Callable<String> second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstResult = executor.submit(() -> { ready.countDown(); assertTrue(start.await(10, TimeUnit.SECONDS)); return first.call(); });
            var secondResult = executor.submit(() -> { ready.countDown(); assertTrue(start.await(10, TimeUnit.SECONDS)); return second.call(); });
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            return List.of(firstResult.get(25, TimeUnit.SECONDS), secondResult.get(25, TimeUnit.SECONDS));
        } finally {
            start.countDown();
        }
    }

    private void assertOccupation(int expected) {
        assertEquals(expected, classes.findById(classId).orElseThrow().getCuposOcupados());
        assertEquals(expected, enrollments.countByGymClassIdAndEstado(classId, "ACTIVO"));
    }
}
