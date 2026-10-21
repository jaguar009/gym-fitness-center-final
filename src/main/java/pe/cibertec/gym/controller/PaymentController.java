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

@Controller @RequestMapping("/payments")
public class PaymentController {
    private final PaymentRepository repository; private final MemberRepository members;
    public PaymentController(PaymentRepository repository,MemberRepository members) {this.repository=repository;this.members=members;}
    @InitBinder("payment") public void binding(org.springframework.web.bind.WebDataBinder binder) {binder.setAllowedFields("id","monto","fechaPago","metodoPago","concepto");}
    @GetMapping public String list(@RequestParam(required=false) java.time.LocalDate from,@RequestParam(required=false) java.time.LocalDate to,@RequestParam(required=false) String method,Model model) {
        var start=from==null?java.time.LocalDate.now().withDayOfMonth(1):from; var end=to==null?java.time.LocalDate.now():to;
        if(end.isBefore(start)) throw new IllegalArgumentException("La fecha final debe ser posterior a la inicial");
        model.addAttribute("payments",repository.search(start,end,method==null||method.isBlank()?null:method));
        model.addAttribute("from",start); model.addAttribute("to",end); model.addAttribute("method",method==null?"":method); return "payments/list";
    }
    @GetMapping("/new") public String form(Model model) { model.addAttribute("payment",new Payment()); model.addAttribute("members",members.findAll()); return "payments/form";}
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model model) {model.addAttribute("payment",repository.findById(id).orElseThrow());model.addAttribute("members",members.findAll());return "payments/form";}
    @PostMapping public String save(@Valid @ModelAttribute("payment") Payment item,BindingResult result,@RequestParam(required=false) Long memberId,Model model,RedirectAttributes flash) {
        var member=memberId==null?java.util.Optional.<Member>empty():members.findById(memberId);
        if(member.isEmpty()) result.reject("miembro","Seleccione un miembro existente");
        
        model.addAttribute("members",members.findAll()); model.addAttribute("selectedMemberId",memberId);
        if(result.hasErrors()) return "payments/form";
        if(item.getId()!=null && !repository.existsById(item.getId())) throw new IllegalArgumentException("El registro no existe");
        item.setMember(member.orElseThrow());repository.save(item);flash.addFlashAttribute("success","Datos guardados correctamente");return "redirect:/payments";
    }
    @PostMapping("/{id}/delete") @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id,RedirectAttributes flash) { repository.deleteById(id); flash.addFlashAttribute("success","Operación completada correctamente");return "redirect:/payments";}
}
