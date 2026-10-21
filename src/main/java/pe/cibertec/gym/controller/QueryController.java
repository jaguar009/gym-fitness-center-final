package pe.cibertec.gym.controller;

import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.repository.PaymentRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;

@Controller
@RequestMapping("/queries")
public class QueryController {
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public QueryController(MemberRepository memberRepository, PaymentRepository paymentRepository) {
        this.memberRepository = memberRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    public String dashboard() {
        return "queries/dashboard";
    }

    @GetMapping("/members")
    public String members(@RequestParam(required = false) String q,
                          @RequestParam(required = false) String status, Model model) {
        model.addAttribute("rows", memberRepository.search(normalize(q), normalize(status)));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("status", status == null ? "" : status);
        return "queries/members";
    }

    @GetMapping("/payments")
    public String payments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String method, Model model) {
        LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("La fecha final debe ser posterior a la inicial");
        }
        model.addAttribute("rows", paymentRepository.search(start, end, normalize(method)));
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        model.addAttribute("method", method == null ? "" : method);
        return "queries/payments";
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
