package pe.cibertec.gym.controller;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cibertec.gym.model.*;
import pe.cibertec.gym.repository.*;
import pe.cibertec.gym.service.EmployeeService;

@Controller @RequestMapping("/employees") @PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {
    private final EmployeeRepository repository;
    private final EmployeeService service;
    public EmployeeController(EmployeeRepository repository,EmployeeService service) { this.repository=repository; this.service=service; }
    @GetMapping public String list(Model model) { model.addAttribute("employees",repository.findAllByOrderByApellidosAscNombresAsc()); return "employees/list"; }
    @GetMapping("/new") public String form(Model model) { model.addAttribute("employee",new Employee()); return "employees/form"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model model) { model.addAttribute("employee",repository.findById(id).orElseThrow()); return "employees/form"; }
    @PostMapping public String save(@Valid @ModelAttribute("employee") Employee employee, BindingResult result, RedirectAttributes flash) {
        if(result.hasErrors()) return "employees/form";
        try {
            service.save(employee);
        } catch (IllegalArgumentException exception) {
            result.rejectValue("dni", "duplicado", exception.getMessage());
            return "employees/form";
        }
        flash.addFlashAttribute("success","Datos guardados correctamente"); return "redirect:/employees";
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasRole('ADMIN')")
    public String deactivate(@PathVariable Long id,RedirectAttributes flash) {
        service.deactivate(id);
        flash.addFlashAttribute("success","Registro desactivado. Se conserva su historial."); return "redirect:/employees";
    }
}
