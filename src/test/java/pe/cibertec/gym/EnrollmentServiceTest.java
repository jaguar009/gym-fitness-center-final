package pe.cibertec.gym;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.cibertec.gym.model.ClassEnrollment;
import pe.cibertec.gym.model.GymClass;
import pe.cibertec.gym.model.Member;
import pe.cibertec.gym.repository.ClassEnrollmentRepository;
import pe.cibertec.gym.repository.GymClassRepository;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.service.EnrollmentService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnrollmentServiceTest {
    private final ClassEnrollmentRepository enrollments = mock(ClassEnrollmentRepository.class);
    private final GymClassRepository classes = mock(GymClassRepository.class);
    private final MemberRepository members = mock(MemberRepository.class);
    private final EnrollmentService service = new EnrollmentService(enrollments, classes, members);
    private GymClass gymClass;
    private Member member;

    @BeforeEach
    void prepareActiveClassAndMember() {
        gymClass = new GymClass();
        gymClass.setId(1L);
        gymClass.setCupos(2);
        member = new Member();
        member.setId(10L);
        when(classes.findByIdForUpdate(1L)).thenReturn(Optional.of(gymClass));
        when(members.findById(10L)).thenReturn(Optional.of(member));
        when(enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(1L)).thenReturn(List.of());
    }

    @Test
    void createsEnrollmentAndUpdatesOccupiedSlots() {
        service.enroll(1L, 10L);
        verify(enrollments).save(argThat(item -> item.getGymClass() == gymClass
                && item.getMember() == member && "ACTIVO".equals(item.getEstado())));
        assertEquals(1, gymClass.getCuposOcupados());
        verify(classes).save(gymClass);
    }

    @Test
    void rejectsDuplicateWithoutWritingEitherRepository() {
        when(enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(1L))
                .thenReturn(List.of(new ClassEnrollment(gymClass, member)));
        var error = assertThrows(IllegalStateException.class, () -> service.enroll(1L, 10L));
        assertEquals("El miembro ya está inscrito en esta clase", error.getMessage());
        verify(enrollments, never()).save(any());
        verify(classes, never()).save(any());
    }

    @Test
    void reactivatesCancelledEnrollmentInsteadOfCreatingAnotherRow() {
        var previous = new ClassEnrollment(gymClass, member);
        previous.setEstado("CANCELADO");
        when(enrollments.findByGymClassIdOrderByFechaInscripcionDescIdDesc(1L)).thenReturn(List.of(previous));
        service.enroll(1L, 10L);
        assertEquals("ACTIVO", previous.getEstado());
        verify(enrollments).save(same(previous));
        assertEquals(1, gymClass.getCuposOcupados());
    }

    @Test
    void rejectsFullClassWithoutWriting() {
        when(enrollments.countByGymClassIdAndEstado(1L, "ACTIVO")).thenReturn(2L);
        var error = assertThrows(IllegalStateException.class, () -> service.enroll(1L, 10L));
        assertEquals("La clase no tiene cupos disponibles", error.getMessage());
        verify(enrollments, never()).save(any());
        verify(classes, never()).save(any());
    }

    @Test
    void rejectsInactiveClass() {
        gymClass.setEstado("INACTIVO");
        assertInactiveRejected();
    }

    @Test
    void rejectsInactiveMember() {
        member.setEstado("INACTIVO");
        assertInactiveRejected();
    }

    private void assertInactiveRejected() {
        var error = assertThrows(IllegalStateException.class, () -> service.enroll(1L, 10L));
        assertEquals("La clase y el miembro deben estar activos", error.getMessage());
        verify(enrollments, never()).save(any());
        verify(classes, never()).save(any());
    }

    @Test
    void rejectsUnknownClassBeforeReadingMember() {
        when(classes.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        var error = assertThrows(IllegalArgumentException.class, () -> service.enroll(1L, 10L));
        assertEquals("La clase no existe", error.getMessage());
        verify(members, never()).findById(any());
        verifyNoInteractions(enrollments);
    }

    @Test
    void rejectsUnknownMember() {
        when(members.findById(10L)).thenReturn(Optional.empty());
        var error = assertThrows(IllegalArgumentException.class, () -> service.enroll(1L, 10L));
        assertEquals("El miembro no existe", error.getMessage());
        verify(enrollments, never()).save(any());
        verify(classes, never()).save(any());
    }

    @Test
    void cancellationFlushesBeforeRecountingOccupiedSlots() {
        var item = new ClassEnrollment(gymClass, member);
        when(enrollments.findById(99L)).thenReturn(Optional.of(item));
        when(enrollments.countByGymClassIdAndEstado(1L, "ACTIVO")).thenReturn(1L);
        service.cancel(1L, 99L);
        assertEquals("CANCELADO", item.getEstado());
        assertEquals(1, gymClass.getCuposOcupados());
        var order = inOrder(classes, enrollments);
        order.verify(classes).findByIdForUpdate(1L);
        order.verify(enrollments).findById(99L);
        order.verify(enrollments).saveAndFlush(item);
        order.verify(enrollments).countByGymClassIdAndEstado(1L, "ACTIVO");
        order.verify(classes).save(gymClass);
    }

    @Test
    void cancellationRejectsEnrollmentFromAnotherClass() {
        var anotherClass = new GymClass();
        anotherClass.setId(2L);
        when(enrollments.findById(99L)).thenReturn(Optional.of(new ClassEnrollment(anotherClass, member)));
        var error = assertThrows(IllegalArgumentException.class, () -> service.cancel(1L, 99L));
        assertEquals("La inscripción no pertenece a esta clase", error.getMessage());
        verify(enrollments, never()).saveAndFlush(any());
        verify(classes, never()).save(any());
    }

    @Test
    void cancellationRejectsUnknownEnrollment() {
        var error = assertThrows(IllegalArgumentException.class, () -> service.cancel(1L, 99L));
        assertEquals("La inscripción no existe", error.getMessage());
        verify(enrollments, never()).saveAndFlush(any());
        verify(classes, never()).save(any());
    }

    @Test
    void cancellationRejectsUnknownClass() {
        when(classes.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        var error = assertThrows(IllegalArgumentException.class, () -> service.cancel(1L, 99L));
        assertEquals("La clase no existe", error.getMessage());
        verifyNoInteractions(enrollments);
    }
}
