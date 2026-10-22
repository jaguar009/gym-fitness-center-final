package pe.cibertec.gym;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import pe.cibertec.gym.model.*;
import pe.cibertec.gym.repository.*;
import pe.cibertec.gym.security.RecordStatePolicy;
import pe.cibertec.gym.service.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MaintenanceServiceTest {
    private final MemberRepository members = mock(MemberRepository.class);
    private final MembershipRepository memberships = mock(MembershipRepository.class);
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final AttendanceRepository attendance = mock(AttendanceRepository.class);
    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final RecordStatePolicy policy = mock(RecordStatePolicy.class);

    @Test
    void newMemberUsesServerRegistrationDate() {
        var input = member();
        input.setRegistrationDate(LocalDate.of(2050, 1, 1));
        new MemberService(members, policy).save(input);
        verify(policy).checkSave(null, "ACTIVO");
        verify(members).save(argThat(saved -> saved != input
                && saved.getRegistrationDate().equals(LocalDate.now()) && "91000001".equals(saved.getDni())));
    }

    @Test
    void memberUpdateLocksAndKeepsOriginalRegistrationDate() {
        var original = member();
        original.setId(1L);
        original.setRegistrationDate(LocalDate.of(2020, 1, 15));
        var input = member();
        input.setId(1L);
        input.setNombres("Nombre editado");
        when(members.findByIdForUpdate(1L)).thenReturn(Optional.of(original));
        new MemberService(members, policy).save(input);
        assertEquals(LocalDate.of(2020, 1, 15), original.getRegistrationDate());
        assertEquals("Nombre editado", original.getNombres());
        verify(members).findByIdForUpdate(1L);
        verify(members).save(same(original));
    }

    @Test
    void duplicateMemberDniNeverWrites() {
        when(members.existsByDni("91000001")).thenReturn(true);
        var error = assertThrows(IllegalArgumentException.class,
                () -> new MemberService(members, policy).save(member()));
        assertEquals("El DNI ya está registrado", error.getMessage());
        verify(members, never()).save(any());
    }

    @Test
    void deniedMemberStateChangeNeverWrites() {
        var input = member();
        input.setEstado("INACTIVO");
        doThrow(new AccessDeniedException("Estado no permitido")).when(policy).checkSave(null, "INACTIVO");
        assertThrows(AccessDeniedException.class, () -> new MemberService(members, policy).save(input));
        verify(members, never()).save(any());
    }

    @Test
    void memberDeactivationPreservesTheRow() {
        var item = member();
        when(members.findByIdForUpdate(1L)).thenReturn(Optional.of(item));
        new MemberService(members, policy).deactivate(1L);
        assertEquals("INACTIVO", item.getEstado());
        verify(members).save(item);
        verify(members, never()).deleteById(any());
    }

    @Test
    void membershipIsAssociatedWithTheSelectedMember() {
        var member = member();
        when(members.findById(1L)).thenReturn(Optional.of(member));
        var input = membership();
        new MembershipService(memberships, members, policy).save(input, 1L);
        verify(memberships).save(argThat(saved -> saved.getMember() == member
                && saved.getCosto().compareTo(new BigDecimal("80.00")) == 0));
    }

    @Test
    void membershipRejectsReversedDatesBeforeWriting() {
        when(members.findById(1L)).thenReturn(Optional.of(member()));
        var input = membership();
        input.setFechaFin(input.getFechaInicio().minusDays(1));
        assertThrows(IllegalArgumentException.class,
                () -> new MembershipService(memberships, members, policy).save(input, 1L));
        verify(memberships, never()).save(any());
    }

    @Test
    void membershipRejectsUnknownMember() {
        assertThrows(IllegalArgumentException.class,
                () -> new MembershipService(memberships, members, policy).save(membership(), 1L));
        verify(memberships, never()).save(any());
    }

    @Test
    void membershipDeactivationUsesLockedRow() {
        var item = membership();
        when(memberships.findByIdForUpdate(1L)).thenReturn(Optional.of(item));
        new MembershipService(memberships, members, policy).deactivate(1L);
        assertEquals("INACTIVO", item.getEstado());
        verify(memberships).save(item);
    }

    @Test
    void paymentUpdateCopiesOnlyTheExpectedFields() {
        var original = new Payment();
        var input = new Payment();
        input.setId(3L);
        input.setMonto(new BigDecimal("90.00"));
        input.setMetodoPago("EFECTIVO");
        var member = member();
        when(members.findById(1L)).thenReturn(Optional.of(member));
        when(payments.findById(3L)).thenReturn(Optional.of(original));
        new PaymentService(payments, members).save(input, 1L);
        assertSame(member, original.getMember());
        assertEquals(new BigDecimal("90.00"), original.getMonto());
        verify(payments).save(same(original));
    }

    @Test
    void paymentRejectsAnUnknownRecordInsteadOfInsertingIt() {
        var input = new Payment();
        input.setId(3L);
        when(members.findById(1L)).thenReturn(Optional.of(member()));
        assertThrows(IllegalArgumentException.class, () -> new PaymentService(payments, members).save(input, 1L));
        verify(payments, never()).save(any());
    }

    @Test
    void attendanceKeepsItsMemberAndTime() {
        var input = new Attendance();
        input.setFecha(LocalDate.of(2026, 10, 21));
        input.setHoraEntrada("09:30");
        var member = member();
        when(members.findById(1L)).thenReturn(Optional.of(member));
        new AttendanceService(attendance, members).save(input, 1L);
        verify(attendance).save(argThat(saved -> saved.getMember() == member
                && "09:30".equals(saved.getHoraEntrada()) && input.getFecha().equals(saved.getFecha())));
    }

    @Test
    void attendanceRejectsMissingMember() {
        assertThrows(IllegalArgumentException.class,
                () -> new AttendanceService(attendance, members).save(new Attendance(), null));
        verify(attendance, never()).save(any());
    }

    @Test
    void duplicateEmployeeDniNeverWrites() {
        var input = new Employee();
        input.setDni("92000001");
        when(employees.existsByDni(input.getDni())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> new EmployeeService(employees).save(input));
        verify(employees, never()).save(any());
    }

    @Test
    void employeeDeactivationPreservesTheRow() {
        var item = new Employee();
        when(employees.findById(1L)).thenReturn(Optional.of(item));
        new EmployeeService(employees).deactivate(1L);
        assertEquals("INACTIVO", item.getEstado());
        verify(employees).save(item);
        verify(employees, never()).deleteById(any());
    }

    private Member member() {
        var item = new Member();
        item.setDni("91000001");
        item.setNombres("Socio");
        item.setApellidos("Prueba");
        return item;
    }

    private Membership membership() {
        var item = new Membership();
        item.setTipo("Mensual");
        item.setCosto(new BigDecimal("80.00"));
        item.setFechaInicio(LocalDate.of(2026, 10, 1));
        item.setFechaFin(LocalDate.of(2026, 11, 1));
        return item;
    }
}
