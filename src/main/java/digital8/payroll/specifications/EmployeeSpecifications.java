package digital8.payroll.specifications;

import digital8.payroll.entities.Employees;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
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

            if (searchQuery != null && !searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("middleName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("employeeNumber")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("email")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("department").get("departmentName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("position").get("positionName")), searchPattern)
                );
                
                predicates.add(searchPredicate);
            }

            if (departmentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("department").get("departmentId"), departmentId));
            }

            if (positionId != null) {
                predicates.add(criteriaBuilder.equal(root.get("position").get("positionId"), positionId));
            }

            if (employmentStatus != null && !employmentStatus.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("employmentStatus"), employmentStatus));
            }

            if (employmentType != null && !employmentType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("employmentType"), employmentType));
            }

            if (payType != null && !payType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("payType"), payType));
            }

            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basicSalary"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basicSalary"), maxSalary));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}