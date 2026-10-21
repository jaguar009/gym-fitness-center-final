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
import pe.cibertec.gym.security.RecordStatePolicy;

@Controller @RequestMapping("/members") 
public class MemberController {
    private final MemberRepository repository;
    private final RecordStatePolicy statePolicy;
    public MemberController(MemberRepository repository, RecordStatePolicy statePolicy) {
        this.repository=repository; this.statePolicy=statePolicy;
    }
    @InitBinder("member") public void binding(org.springframework.web.bind.WebDataBinder binder) {
        binder.setAllowedFields("id","dni","nombres","apellidos","telefono","correo","direccion","estado");
    }
    @GetMapping public String list(@RequestParam(required=false) String q, @RequestParam(required=false) String status, Model model) {
        model.addAttribute("members", repository.search(q==null || q.isBlank()? null:q.trim(),status==null || status.isBlank()? null:status));
        model.addAttribute("q",q==null?"":q); model.addAttribute("status",status==null?"":status); return "members/list";
    }
    @GetMapping("/new") public String form(Model model) { model.addAttribute("member",new Member()); return "members/form"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model model) { model.addAttribute("member",repository.findById(id).orElseThrow()); return "members/form"; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','RECEPCION')")
    public String save(@Valid @ModelAttribute("member") Member member, BindingResult result, RedirectAttributes flash) {
        if (!result.hasFieldErrors("dni") && (member.getId()==null? repository.existsByDni(member.getDni()):repository.existsByDniAndIdNot(member.getDni(),member.getId()))) result.rejectValue("dni","duplicado","El DNI ya está registrado");
        if(result.hasErrors()) return "members/form";
        var current=member.getId()==null?null:repository.findById(member.getId()).orElseThrow();
        statePolicy.checkSave(current==null?null:current.getEstado(),member.getEstado());
        if (current!=null) member.setRegistrationDate(current.getRegistrationDate());
        repository.save(member); flash.addFlashAttribute("success","Datos guardados correctamente"); return "redirect:/members";
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasRole('ADMIN')")
    public String deactivate(@PathVariable Long id,RedirectAttributes flash) {
        var item=repository.findById(id).orElseThrow(); item.setEstado("INACTIVO"); repository.save(item);
        flash.addFlashAttribute("success","Registro desactivado. Se conserva su historial."); return "redirect:/members";
    }
}
