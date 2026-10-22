package pe.cibertec.gym.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.gym.model.Attendance;
import pe.cibertec.gym.repository.AttendanceRepository;
import pe.cibertec.gym.repository.MemberRepository;

@Service
public class AttendanceService {
    private final AttendanceRepository attendance;
    private final MemberRepository members;

    public AttendanceService(AttendanceRepository attendance, MemberRepository members) {
        this.attendance = attendance;
        this.members = members;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public void save(Attendance input, Long memberId) {
        if (memberId == null) throw new IllegalArgumentException("Seleccione un miembro existente");
        var member = members.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Seleccione un miembro existente"));
        Attendance item = input.getId() == null ? new Attendance() : attendance.findById(input.getId())
                .orElseThrow(() -> new IllegalArgumentException("El registro no existe"));
        item.setMember(member);
        item.setFecha(input.getFecha());
        item.setHoraEntrada(input.getHoraEntrada());
        item.setObservacion(input.getObservacion());
        attendance.save(item);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        attendance.deleteById(id);
    }
}
