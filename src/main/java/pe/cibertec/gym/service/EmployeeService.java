package pe.cibertec.gym.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.gym.model.Employee;
import pe.cibertec.gym.repository.EmployeeRepository;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeService {
    private final EmployeeRepository employees;

    public EmployeeService(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Transactional
    public void save(Employee input) {
        boolean duplicate = input.getId() == null ? employees.existsByDni(input.getDni())
                : employees.existsByDniAndIdNot(input.getDni(), input.getId());
        if (duplicate) throw new IllegalArgumentException("El DNI ya está registrado");
        Employee item = input.getId() == null ? new Employee() : employees.findById(input.getId()).orElseThrow();
        item.setDni(input.getDni());
        item.setNombres(input.getNombres());
        item.setApellidos(input.getApellidos());
        item.setCargo(input.getCargo());
        item.setTelefono(input.getTelefono());
        item.setCorreo(input.getCorreo());
        item.setEstado(input.getEstado());
        employees.save(item);
    }

    @Transactional
    public void deactivate(Long id) {
        Employee item = employees.findById(id).orElseThrow();
        item.setEstado("INACTIVO");
        employees.save(item);
    }
}
