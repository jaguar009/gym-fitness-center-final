package pe.cibertec.gym;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.gym.model.GymClass;
import pe.cibertec.gym.model.Member;
import pe.cibertec.gym.model.Membership;
import pe.cibertec.gym.repository.GymClassRepository;
import pe.cibertec.gym.repository.MemberRepository;
import pe.cibertec.gym.repository.MembershipRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StateAuthorizationTest {
    @Autowired private MockMvc mvc;
    @Autowired private MemberRepository members;
    @Autowired private MembershipRepository memberships;
    @Autowired private GymClassRepository classes;

    static Stream<Arguments> changes() {
        return Stream.of("members","memberships","classes").flatMap(module -> Stream.of(
                Arguments.of(module,"RECEPCION","ACTIVO","INACTIVO",403),
                Arguments.of(module,"RECEPCION","INACTIVO","ACTIVO",403),
                Arguments.of(module,"RECEPCION","ACTIVO","ACTIVO",302),
                Arguments.of(module,"RECEPCION","INACTIVO","INACTIVO",302),
                Arguments.of(module,"ADMIN","ACTIVO","INACTIVO",302),
                Arguments.of(module,"ADMIN","INACTIVO","ACTIVO",302)));
    }

    @ParameterizedTest(name="{0} {1}: {2} a {3}")
    @MethodSource("changes")
    void stateChangesRequireAdministrator(String module,String role,String before,String after,int expected) throws Exception {
        Member member=member();
        Long id;
        if ("members".equals(module)) {
            member.setEstado(before);members.saveAndFlush(member);id=member.getId();
        } else if ("memberships".equals(module)) {
            Membership item=new Membership();item.setMember(member);item.setTipo("Mensual");
            item.setCosto(new BigDecimal("80.00"));item.setFechaInicio(LocalDate.of(2026,10,1));
            item.setFechaFin(LocalDate.of(2026,11,1));item.setEstado(before);
            id=memberships.saveAndFlush(item).getId();
        } else {
            GymClass item=new GymClass();item.setNombre("Clase prueba");item.setCupos(10);item.setEstado(before);
            id=classes.saveAndFlush(item).getId();
        }
        mvc.perform(form(module,member).param("id",id.toString()).param("estado",after)
                        .with(user("prueba").roles(role)).with(csrf()))
                .andExpect(status().is(expected));
        assertEquals(expected==403?before:after,state(module,id));
    }

    static Stream<Arguments> creations() {
        return Stream.of("members","memberships","classes").flatMap(module -> Stream.of(
                Arguments.of(module,"RECEPCION","INACTIVO",403),
                Arguments.of(module,"RECEPCION","ACTIVO",302),
                Arguments.of(module,"ADMIN","INACTIVO",302)));
    }

    @ParameterizedTest(name="Crear {0} como {1} con estado {2}")
    @MethodSource("creations")
    void receptionCanOnlyCreateActiveRecords(String module,String role,String state,int expected) throws Exception {
        Member member=member();
        long before=count(module);
        var request="members".equals(module)
                ? post("/members").param("dni","90000002").param("nombres","Socio nuevo").param("apellidos","Prueba")
                : form(module,member);
        request.param("estado",state);
        mvc.perform(request.with(user("prueba").roles(role)).with(csrf()))
                .andExpect(status().is(expected));
        assertEquals(before+(expected==302?1:0),count(module));
    }

    private Member member() {
        Member item=new Member();item.setDni("90000001");item.setNombres("Socio");item.setApellidos("Prueba");
        return members.saveAndFlush(item);
    }
    private MockHttpServletRequestBuilder form(String module,Member member) {
        var request=post("/"+module);
        if ("members".equals(module))return request.param("dni",member.getDni()).param("nombres","Socio editado").param("apellidos","Prueba");
        if ("memberships".equals(module))return request.param("memberId",member.getId().toString()).param("tipo","Mensual")
                .param("costo","80.00").param("fechaInicio","2026-10-01").param("fechaFin","2026-11-01");
        return request.param("nombre","Clase editada").param("cupos","10");
    }
    private String state(String module,Long id) {
        if ("members".equals(module))return members.findById(id).orElseThrow().getEstado();
        if ("memberships".equals(module))return memberships.findById(id).orElseThrow().getEstado();
        return classes.findById(id).orElseThrow().getEstado();
    }
    private long count(String module) {
        if ("members".equals(module))return members.count();
        if ("memberships".equals(module))return memberships.count();
        return classes.count();
    }
}
