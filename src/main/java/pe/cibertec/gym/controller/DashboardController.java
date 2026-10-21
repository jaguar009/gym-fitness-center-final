package pe.cibertec.gym.controller;

import pe.cibertec.gym.repository.AttendanceRepository;
import pe.cibertec.gym.repository.GymClassRepository;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.repository.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final GymClassRepository classRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardController(MemberRepository memberRepository, PaymentRepository paymentRepository,
                               GymClassRepository classRepository, AttendanceRepository attendanceRepository) {
        this.memberRepository = memberRepository;
        this.paymentRepository = paymentRepository;
        this.classRepository = classRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("memberCount", memberRepository.count());
        model.addAttribute("activeMemberCount", memberRepository.countByEstado("ACTIVO"));
        model.addAttribute("classCount", classRepository.countByEstado("ACTIVO"));
        model.addAttribute("attendanceCount", attendanceRepository.count());
        model.addAttribute("income", paymentRepository.totalAmount());
        return "dashboard";
    }
}
