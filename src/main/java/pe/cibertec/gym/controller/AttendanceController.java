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

@Controller @RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceRepository repository; private final MemberRepository members;
    public AttendanceController(AttendanceRepository repository,MemberRepository members) {this.repository=repository;this.members=members;}
    @InitBinder("attendanceItem") public void binding(org.springframework.web.bind.WebDataBinder binder) {binder.setAllowedFields("id","fecha","horaEntrada","observacion");}
    @GetMapping public String list(Model model) { model.addAttribute("attendance",repository.findAllByOrderByFechaDescIdDesc()); return "attendance/list"; }
    @GetMapping("/new") public String form(Model model) { model.addAttribute("attendanceItem",new Attendance()); model.addAttribute("members",members.findAll()); return "attendance/form";}
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model model) {model.addAttribute("attendanceItem",repository.findById(id).orElseThrow());model.addAttribute("members",members.findAll());return "attendance/form";}
    @PostMapping public String save(@Valid @ModelAttribute("attendanceItem") Attendance item,BindingResult result,@RequestParam(required=false) Long memberId,Model model,RedirectAttributes flash) {
        var member=memberId==null?java.util.Optional.<Member>empty():members.findById(memberId);
        if(member.isEmpty()) result.reject("miembro","Seleccione un miembro existente");
        
        model.addAttribute("members",members.findAll()); model.addAttribute("selectedMemberId",memberId);
        if(result.hasErrors()) return "attendance/form";
        if(item.getId()!=null && !repository.existsById(item.getId())) throw new IllegalArgumentException("El registro no existe");
        item.setMember(member.orElseThrow());repository.save(item);flash.addFlashAttribute("success","Datos guardados correctamente");return "redirect:/attendance";
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id,RedirectAttributes flash) { repository.deleteById(id); flash.addFlashAttribute("success","Operación completada correctamente");return "redirect:/attendance";}
}
