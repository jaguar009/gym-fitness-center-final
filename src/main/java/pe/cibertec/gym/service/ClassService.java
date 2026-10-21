package pe.cibertec.gym.service;

import pe.cibertec.gym.model.GymClass;
import pe.cibertec.gym.repository.EmployeeRepository;
import pe.cibertec.gym.repository.GymClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.gym.security.RecordStatePolicy;

@Service
public class ClassService {
    private final GymClassRepository classes;
    private final EmployeeRepository employees;
    private final RecordStatePolicy statePolicy;
    public ClassService(GymClassRepository classes, EmployeeRepository employees, RecordStatePolicy statePolicy) {
        this.classes = classes; this.employees = employees; this.statePolicy=statePolicy;
    }
    @Transactional
    public void save(GymClass input, Long employeeId) {
        GymClass item = input.getId() == null ? new GymClass() : classes.findByIdForUpdate(input.getId())
                .orElseThrow(() -> new IllegalArgumentException("La clase no existe"));
        statePolicy.checkSave(input.getId()==null?null:item.getEstado(),input.getEstado());
        if (input.getCupos() < item.getCuposOcupados()) {
            throw new IllegalArgumentException("La capacidad no puede ser menor que los cupos ocupados");
        }
        item.setNombre(input.getNombre()); item.setDescripcion(input.getDescripcion());
        item.setHorario(input.getHorario()); item.setCupos(input.getCupos()); item.setEstado(input.getEstado());
        item.setEmployee(employeeId == null ? null : employees.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("El empleado no existe")));
        classes.save(item);
    }
    @Transactional
    public void deactivate(Long id) {
        var item = classes.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("La clase no existe"));
        item.setEstado("INACTIVO"); classes.save(item);
    }
}
