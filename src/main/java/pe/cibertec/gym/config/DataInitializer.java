package pe.cibertec.gym.config;

import pe.cibertec.gym.model.AppUser;
import pe.cibertec.gym.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(
            AppUserRepository repository,
            PasswordEncoder encoder,
            @Value("${app.bootstrap.admin-password}") String adminPassword,
            @Value("${app.bootstrap.reception-password}") String receptionPassword) {
        return args -> {
            if (repository.findByUsername("admin").isEmpty()) {
                repository.save(new AppUser("admin", encoder.encode(adminPassword), "ADMIN", "ACTIVO"));
            }
            if (repository.findByUsername("recepcion").isEmpty()) {
                repository.save(new AppUser("recepcion", encoder.encode(receptionPassword), "RECEPCION", "ACTIVO"));
            }
        };
    }
}
