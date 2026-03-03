package digital8.payroll.repositories;

import digital8.payroll.entities.Employees;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Custom repository methods to avoid N+1 when loading employees with relations.
 */
public interface EmployeeRepositoryCustom {

    /**
     * Same as findAll(spec, sort) but uses a single query with JOIN FETCH
     * for department, position, and user so all data is loaded in one round-trip.
     */
    List<Employees> findAllWithFetch(Specification<Employees> spec, Sort sort);
}
