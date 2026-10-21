package pe.cibertec.gym;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.gym.model.Member;
import pe.cibertec.gym.repository.EmployeeRepository;
import pe.cibertec.gym.repository.MemberRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HttpSecurityTest {
    @Autowired private MockMvc mvc;
    @Autowired private MemberRepository members;
    @Autowired private EmployeeRepository employees;

    @ParameterizedTest
    @ValueSource(strings = {"/members", "/memberships", "/payments", "/attendance", "/classes", "/employees"})
    void writesWithoutCsrfTokenAreRejected(String path) throws Exception {
        mvc.perform(post(path).with(user("administrador").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/members", "/memberships", "/payments", "/attendance", "/classes", "/employees"})
    void writesWithInvalidCsrfTokenAreRejected(String path) throws Exception {
        mvc.perform(post(path).with(user("administrador").roles("ADMIN")).with(csrf().useInvalidToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void validFormAndCsrfTokenCanCreateMember() throws Exception {
        long before = members.count();
        mvc.perform(post("/members").with(user("administrador").roles("ADMIN")).with(csrf())
                        .param("dni", "90200001").param("nombres", "Prueba").param("apellidos", "Seguridad")
                        .param("estado", "ACTIVO"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/members"));
        assertEquals(before + 1, members.count());
    }

    @Test
    void receptionCannotManageEmployeesEvenWithValidCsrf() throws Exception {
        long before = employees.count();
        mvc.perform(get("/employees").with(user("recepcion").roles("RECEPCION")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/employees").with(user("recepcion").roles("RECEPCION")).with(csrf())
                        .param("dni", "90200002").param("nombres", "Prueba").param("apellidos", "Seguridad")
                        .param("estado", "ACTIVO"))
                .andExpect(status().isForbidden());
        assertEquals(before, employees.count());
    }

    @Test
    void administratorCanOpenEmployeeForm() throws Exception {
        mvc.perform(get("/employees/new").with(user("administrador").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(view().name("employees/form"));
    }

    @Test
    void memberRegistrationDateCannotBeReplacedFromAForm() throws Exception {
        var member = new Member();
        member.setDni("90200003");
        member.setNombres("Socio");
        member.setApellidos("Prueba");
        member.setRegistrationDate(LocalDate.of(2020, 1, 15));
        members.saveAndFlush(member);
        mvc.perform(post("/members").with(user("administrador").roles("ADMIN")).with(csrf())
                        .param("id", member.getId().toString()).param("dni", member.getDni())
                        .param("nombres", "Socio editado").param("apellidos", "Prueba").param("estado", "ACTIVO")
                        .param("registrationDate", "2050-01-01"))
                .andExpect(status().is3xxRedirection());
        assertEquals(LocalDate.of(2020, 1, 15), members.findById(member.getId()).orElseThrow().getRegistrationDate());
    }
}
