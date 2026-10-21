package pe.cibertec.gym.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Long id;
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    @Column(name = "rol", nullable = false, length = 30)
    private String rol;
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    public AppUser() { }
    public AppUser(String username, String password, String role, String status) {
        this.username = username;
        this.password = password;
        this.rol = role;
        this.estado = status;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return rol; }
    public void setRole(String role) { this.rol = role; }
    public String getStatus() { return estado; }
    public void setStatus(String status) { this.estado = status; }
}
