package pe.cibertec.gym.controller;

import pe.cibertec.gym.model.GymClass;
import pe.cibertec.gym.repository.EmployeeRepository;
import pe.cibertec.gym.repository.GymClassRepository;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.service.EnrollmentService;
import pe.cibertec.gym.service.ClassService;
import pe.cibertec.gym.repository.ClassEnrollmentRepository;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/classes")
public class ClassController {
    private final GymClassRepository repository;
    private final EmployeeRepository employeeRepository;
    private final MemberRepository memberRepository;
    private final EnrollmentService enrollmentService;
    private final ClassService classService;
    private final ClassEnrollmentRepository enrollmentRepository;

    public ClassController(GymClassRepository repository, EmployeeRepository employeeRepository,
                           MemberRepository memberRepository, EnrollmentService enrollmentService,
                           ClassService classService, ClassEnrollmentRepository enrollmentRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.memberRepository = memberRepository;
        this.enrollmentService = enrollmentService;
        this.classService = classService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @InitBinder("gymClass")
    public void binding(WebDataBinder binder) {
        binder.setAllowedFields("id", "nombre", "descripcion", "horario", "cupos", "estado");
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("classes", repository.findAllByOrderByNombreAsc());
        return "classes/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public String form(Model model) {
        model.addAttribute("gymClass", new GymClass());
        model.addAttribute("employees", employeeRepository.findAll());
        return "classes/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public String save(@Valid @ModelAttribute("gymClass") GymClass gymClass, BindingResult result,
                       @RequestParam(required = false) Long employeeId, Model model, RedirectAttributes attributes) {
        model.addAttribute("employees", employeeRepository.findAll());
        if (result.hasErrors()) return "classes/form";
        try {
            classService.save(gymClass, employeeId);
        } catch (IllegalArgumentException ex) {
            result.reject("clase", ex.getMessage());
            return "classes/form";
        }
        attributes.addFlashAttribute("success", "Clase guardada correctamente");
        return "redirect:/classes";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("gymClass", repository.findById(id).orElseThrow());
        model.addAttribute("employees", employeeRepository.findAll());
        return "classes/form";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id) {
        classService.deactivate(id);
        return "redirect:/classes";
    }

    @GetMapping("/{id}/enroll")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public String enrollmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("gymClass", repository.findById(id).orElseThrow());
        model.addAttribute("members", memberRepository.findAll());
        model.addAttribute("enrollments", enrollmentRepository.findByGymClassIdOrderByFechaInscripcionDescIdDesc(id));
        return "classes/enroll";
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public String enroll(@PathVariable Long id, @RequestParam Long memberId, RedirectAttributes attributes) {
        try {
            enrollmentService.enroll(id, memberId);
            attributes.addFlashAttribute("success", "Inscripción registrada correctamente");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/classes";
    }

    @PostMapping("/{id}/enrollments/{enrollmentId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public String cancel(@PathVariable Long id, @PathVariable Long enrollmentId, RedirectAttributes attributes) {
        enrollmentService.cancel(id, enrollmentId);
        attributes.addFlashAttribute("success", "Inscripción cancelada y cupo liberado");
        return "redirect:/classes/" + id + "/enroll";
    }
}
