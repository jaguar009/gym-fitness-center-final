package pe.cibertec.gym;

import pe.cibertec.gym.model.Member;
import pe.cibertec.gym.model.Payment;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QueryControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private MemberRepository members;
    @Autowired private PaymentRepository payments;

    @Test
    void queriesRequireAuthentication() throws Exception {
        for (String path : new String[]{"/queries", "/queries/members", "/queries/payments"}) {
            mvc.perform(get(path)).andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administratorCanReadQueries() throws Exception {
        verifyQueryPages();
    }

    @Test
    @WithMockUser(roles = "RECEPCION")
    void receptionCanReadQueries() throws Exception {
        verifyQueryPages();
    }

    @Test
    @WithMockUser(roles = "RECEPCION")
    void membersCanBeFilteredByNameAndState() throws Exception {
        member("90000001", "Maria", "ACTIVO");
        member("90000002", "Maria", "INACTIVO");
        member("90000003", "Pedro", "ACTIVO");
        mvc.perform(get("/queries/members").param("q", " Maria ").param("status", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("rows", hasSize(1)))
                .andExpect(content().string(containsString("90000001")))
                .andExpect(content().string(not(containsString("90000002"))))
                .andExpect(content().string(not(containsString("90000003"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void membersCanBeFilteredByDni() throws Exception {
        member("90000001", "Maria", "ACTIVO");
        member("90000002", "Pedro", "ACTIVO");
        mvc.perform(get("/queries/members").param("q", "90000002"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("rows", hasSize(1)))
                .andExpect(content().string(containsString("Pedro Lopez")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void emptySearchRendersAnEmptyState() throws Exception {
        mvc.perform(get("/queries/members").param("q", "no-existe-90009999"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("rows", empty()))
                .andExpect(content().string(containsString("Sin resultados.")));
    }

    @Test
    @WithMockUser(roles = "RECEPCION")
    void paymentsKeepTheirDateAndMethodFilters() throws Exception {
        Member member = member("90000001", "Maria", "ACTIVO");
        payment(member, LocalDate.of(2026, 10, 10), "YAPE", "En periodo");
        payment(member, LocalDate.of(2026, 10, 10), "EFECTIVO", "Otro metodo");
        payment(member, LocalDate.of(2026, 9, 30), "YAPE", "Fuera de periodo");
        mvc.perform(get("/queries/payments").param("from", "2026-10-01")
                        .param("to", "2026-10-14").param("method", "YAPE"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("rows", hasSize(1)))
                .andExpect(content().string(containsString("En periodo")))
                .andExpect(content().string(not(containsString("Otro metodo"))))
                .andExpect(content().string(not(containsString("Fuera de periodo"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void paymentsDefaultToTheCurrentMonth() throws Exception {
        mvc.perform(get("/queries/payments"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("from", LocalDate.now().withDayOfMonth(1)))
                .andExpect(model().attribute("to", LocalDate.now()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reversedDatesAreRejected() throws Exception {
        mvc.perform(get("/queries/payments").param("from", "2026-10-14").param("to", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser(roles = "RECEPCION")
    void malformedDatesAreRejected() throws Exception {
        mvc.perform(get("/queries/payments").param("from", "no-es-fecha"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboardLinksToQueriesWithoutPdfActions() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/queries\"")))
                .andExpect(content().string(not(containsString("/reports"))))
                .andExpect(content().string(not(containsString("Descargar PDF"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removedReportAndPdfRoutesDoNotExist() throws Exception {
        for (String path : new String[]{"/reports", "/reports/members", "/reports/payments",
                "/reports/members/pdf", "/reports/payments/pdf",
                "/queries/members/pdf", "/queries/payments/pdf"}) {
            mvc.perform(get(path)).andExpect(status().isNotFound());
        }
    }

    private void verifyQueryPages() throws Exception {
        for (String path : new String[]{"/queries", "/queries/members", "/queries/payments"}) {
            mvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("/reports"))))
                    .andExpect(content().string(not(containsString("Descargar PDF"))));
        }
    }

    private Member member(String dni, String name, String state) {
        Member member = new Member();
        member.setDni(dni);
        member.setNombres(name);
        member.setApellidos("Lopez");
        member.setEstado(state);
        return members.saveAndFlush(member);
    }

    private void payment(Member member, LocalDate date, String method, String concept) {
        Payment payment = new Payment();
        payment.setMember(member);
        payment.setMonto(new BigDecimal("120.00"));
        payment.setFechaPago(date);
        payment.setMetodoPago(method);
        payment.setConcepto(concept);
        payments.saveAndFlush(payment);
    }
}
