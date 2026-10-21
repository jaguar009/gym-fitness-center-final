package pe.cibertec.gym.repository;

import pe.cibertec.gym.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);
    List<Employee> findAllByOrderByApellidosAscNombresAsc();
}
