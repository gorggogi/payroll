package digital8.payroll.specifications;

import digital8.payroll.entities.Employees;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecifications {

    public static Specification<Employees> filterBy(
            String searchQuery,
            Integer departmentId,
            Integer positionId,
            String employmentStatus,
            String employmentType,
            String payType,
            BigDecimal minSalary,
            BigDecimal maxSalary) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search across multiple fields (OR condition)
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("middleName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("employeeNumber")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("department").get("departmentName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("position").get("positionName")), searchPattern)
                );
                
                predicates.add(searchPredicate);
            }

            // Filter by department (AND condition)
            if (departmentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("department").get("departmentId"), departmentId));
            }

            // Filter by position
            if (positionId != null) {
                predicates.add(criteriaBuilder.equal(root.get("position").get("positionId"), positionId));
            }

            // Filter by employment status
            if (employmentStatus != null && !employmentStatus.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("employmentStatus"), employmentStatus));
            }

            // Filter by employment type
            if (employmentType != null && !employmentType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("employmentType"), employmentType));
            }

            // Filter by pay type
            if (payType != null && !payType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("payType"), payType));
            }

            // Filter by minimum salary
            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basicSalary"), minSalary));
            }

            // Filter by maximum salary
            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basicSalary"), maxSalary));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}