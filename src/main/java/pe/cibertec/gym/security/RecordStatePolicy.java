package pe.cibertec.gym.security;

import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RecordStatePolicy {
    public void checkSave(String currentState, String requestedState) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean administrator = authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        boolean reception = authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_RECEPCION".equals(authority.getAuthority()));
        String permittedState = currentState == null ? "ACTIVO" : currentState;
        if (!administrator && (!reception || !Objects.equals(permittedState, requestedState))) {
            throw new AccessDeniedException("Solo ADMIN puede cambiar el estado del registro");
        }
    }
}
