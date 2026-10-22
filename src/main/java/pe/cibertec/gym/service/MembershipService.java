package pe.cibertec.gym.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.gym.model.Membership;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.repository.MembershipRepository;
import pe.cibertec.gym.security.RecordStatePolicy;

@Service
public class MembershipService {
    private final MembershipRepository memberships;
    private final MemberRepository members;
    private final RecordStatePolicy statePolicy;

    public MembershipService(MembershipRepository memberships, MemberRepository members, RecordStatePolicy statePolicy) {
        this.memberships = memberships;
        this.members = members;
        this.statePolicy = statePolicy;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public void save(Membership input, Long memberId) {
        if (memberId == null) throw new IllegalArgumentException("Seleccione un miembro existente");
        var member = members.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Seleccione un miembro existente"));
        if (input.getFechaInicio() == null || input.getFechaFin() == null
                || input.getFechaFin().isBefore(input.getFechaInicio())) {
            throw new IllegalArgumentException("Revise el rango de fechas de la membresía");
        }
        Membership item = input.getId() == null ? new Membership()
                : memberships.findByIdForUpdate(input.getId()).orElseThrow();
        statePolicy.checkSave(input.getId() == null ? null : item.getEstado(), input.getEstado());
        item.setMember(member);
        item.setTipo(input.getTipo());
        item.setCosto(input.getCosto());
        item.setFechaInicio(input.getFechaInicio());
        item.setFechaFin(input.getFechaFin());
        item.setEstado(input.getEstado());
        memberships.save(item);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(Long id) {
        Membership item = memberships.findByIdForUpdate(id).orElseThrow();
        item.setEstado("INACTIVO");
        memberships.save(item);
    }
}
