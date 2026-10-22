package pe.cibertec.gym.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.gym.model.Member;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.security.RecordStatePolicy;

@Service
public class MemberService {
    private final MemberRepository members;
    private final RecordStatePolicy statePolicy;

    public MemberService(MemberRepository members, RecordStatePolicy statePolicy) {
        this.members = members;
        this.statePolicy = statePolicy;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public void save(Member input) {
        Member item = input.getId() == null ? new Member()
                : members.findByIdForUpdate(input.getId()).orElseThrow();
        statePolicy.checkSave(input.getId() == null ? null : item.getEstado(), input.getEstado());
        boolean duplicate = input.getId() == null ? members.existsByDni(input.getDni())
                : members.existsByDniAndIdNot(input.getDni(), input.getId());
        if (duplicate) throw new IllegalArgumentException("El DNI ya está registrado");
        item.setDni(input.getDni());
        item.setNombres(input.getNombres());
        item.setApellidos(input.getApellidos());
        item.setTelefono(input.getTelefono());
        item.setCorreo(input.getCorreo());
        item.setDireccion(input.getDireccion());
        item.setEstado(input.getEstado());
        members.save(item);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(Long id) {
        Member item = members.findByIdForUpdate(id).orElseThrow();
        item.setEstado("INACTIVO");
        members.save(item);
    }
}
