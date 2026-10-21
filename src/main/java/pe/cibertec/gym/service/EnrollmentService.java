package pe.cibertec.gym.service;

import pe.cibertec.gym.model.ClassEnrollment;
import pe.cibertec.gym.repository.ClassEnrollmentRepository;
import pe.cibertec.gym.repository.GymClassRepository;
import pe.cibertec.gym.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
    private final ClassEnrollmentRepository enrollmentRepository;
    private final GymClassRepository classRepository;
    private final MemberRepository memberRepository;

    public EnrollmentService(ClassEnrollmentRepository enrollmentRepository, GymClassRepository classRepository,
                             MemberRepository memberRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.classRepository = classRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void enroll(Long classId, Long memberId) {
        var gymClass = classRepository.findByIdForUpdate(classId)
                .orElseThrow(() -> new IllegalArgumentException("La clase no existe"));
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("El miembro no existe"));

        if (!"ACTIVO".equals(gymClass.getEstado()) || !"ACTIVO".equals(member.getEstado())) {
            throw new IllegalStateException("La clase y el miembro deben estar activos");
        }
        long occupiedSlots = enrollmentRepository.countByGymClassIdAndEstado(classId, "ACTIVO");
        if (occupiedSlots >= gymClass.getCupos()) {
            throw new IllegalStateException("La clase no tiene cupos disponibles");
        }
        var previous = enrollmentRepository.findByGymClassIdOrderByFechaInscripcionDescIdDesc(classId).stream()
                .filter(e -> e.getMember().getId().equals(memberId)).findFirst();
        if (previous.isPresent() && "ACTIVO".equals(previous.get().getEstado())) {
            throw new IllegalStateException("El miembro ya está inscrito en esta clase");
        }
        if (previous.isPresent()) {
            previous.get().setEstado("ACTIVO");
            enrollmentRepository.save(previous.get());
        } else {
            enrollmentRepository.save(new ClassEnrollment(gymClass, member));
        }
        gymClass.setCuposOcupados(Math.toIntExact(occupiedSlots + 1));
        classRepository.save(gymClass);
    }

    @Transactional
    public void cancel(Long classId, Long enrollmentId) {
        var gymClass = classRepository.findByIdForUpdate(classId)
                .orElseThrow(() -> new IllegalArgumentException("La clase no existe"));
        var enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("La inscripción no existe"));
        if (!enrollment.getGymClass().getId().equals(classId)) {
            throw new IllegalArgumentException("La inscripción no pertenece a esta clase");
        }
        enrollment.setEstado("CANCELADO");
        enrollmentRepository.saveAndFlush(enrollment);
        gymClass.setCuposOcupados(Math.toIntExact(
                enrollmentRepository.countByGymClassIdAndEstado(classId, "ACTIVO")));
        classRepository.save(gymClass);
    }
}
