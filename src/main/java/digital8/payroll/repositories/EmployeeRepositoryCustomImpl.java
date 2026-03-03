package digital8.payroll.repositories;

import digital8.payroll.entities.Employees;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes the same filtered/sorted employee query as findAll(spec, sort)
 * but with JOIN FETCH for department, position, and user so only one
 * database round-trip is made (avoids N+1 and 20s load times).
 */
@Repository
public class EmployeeRepositoryCustomImpl implements EmployeeRepositoryCustom {

    private final EntityManager entityManager;

    public EmployeeRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Employees> findAllWithFetch(Specification<Employees> spec, Sort sort) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employees> cq = cb.createQuery(Employees.class);
        Root<Employees> root = cq.from(Employees.class);

        // Load relations in one query (avoids N+1). User is LEFT so employees without account are still returned.
        root.fetch("department");
        root.fetch("position");
        root.fetch("user", JoinType.LEFT);

        if (spec != null) {
            cq.where(spec.toPredicate(root, cq, cb));
        }
        cq.distinct(true);

        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : sort) {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            }
            cq.orderBy(orders);
        }

        TypedQuery<Employees> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}
