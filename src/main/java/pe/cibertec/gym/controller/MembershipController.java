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
import pe.cibertec.gym.service.MembershipService;

@Controller @RequestMapping("/memberships")
public class MembershipController {
    private final MembershipRepository repository; private final MemberRepository members;
    private final MembershipService service;
    public MembershipController(MembershipRepository repository,MemberRepository members,MembershipService service) {
        this.repository=repository;this.members=members;this.service=service;
    }
    @InitBinder("membership") public void binding(org.springframework.web.bind.WebDataBinder binder) {binder.setAllowedFields("id","tipo","costo","fechaInicio","fechaFin","estado");}
    @GetMapping public String list(Model model) { model.addAttribute("memberships",repository.findAllByOrderByFechaFinAsc()); return "memberships/list"; }
    @GetMapping("/new") public String form(Model model) { model.addAttribute("membership",new Membership()); model.addAttribute("members",members.findAll()); return "memberships/form";}
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model model) {model.addAttribute("membership",repository.findById(id).orElseThrow());model.addAttribute("members",members.findAll());return "memberships/form";}
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public String save(@Valid @ModelAttribute("membership") Membership item,BindingResult result,@RequestParam(required=false) Long memberId,Model model,RedirectAttributes flash) {
        var member=memberId==null?java.util.Optional.<Member>empty():members.findById(memberId);
        if(member.isEmpty()) result.reject("miembro","Seleccione un miembro existente");
        if(item.getFechaInicio()!=null && item.getFechaFin()!=null && item.getFechaFin().isBefore(item.getFechaInicio())) result.rejectValue("fechaFin","rango","La fecha final debe ser posterior a la inicial");
        model.addAttribute("members",members.findAll()); model.addAttribute("selectedMemberId",memberId);
        if(result.hasErrors()) return "memberships/form";
        service.save(item,memberId);flash.addFlashAttribute("success","Datos guardados correctamente");return "redirect:/memberships";
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id,RedirectAttributes flash) { service.deactivate(id); flash.addFlashAttribute("success","Operación completada correctamente");return "redirect:/memberships";}
}
