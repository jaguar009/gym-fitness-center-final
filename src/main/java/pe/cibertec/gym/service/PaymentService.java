package pe.cibertec.gym.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.gym.model.Payment;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.repository.PaymentRepository;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final MemberRepository members;

    public PaymentService(PaymentRepository payments, MemberRepository members) {
        this.payments = payments;
        this.members = members;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public void save(Payment input, Long memberId) {
        if (memberId == null) throw new IllegalArgumentException("Seleccione un miembro existente");
        var member = members.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Seleccione un miembro existente"));
        Payment item = input.getId() == null ? new Payment() : payments.findById(input.getId())
                .orElseThrow(() -> new IllegalArgumentException("El registro no existe"));
        item.setMember(member);
        item.setMonto(input.getMonto());
        item.setFechaPago(input.getFechaPago());
        item.setMetodoPago(input.getMetodoPago());
        item.setConcepto(input.getConcepto());
        payments.save(item);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        payments.deleteById(id);
    }
}
