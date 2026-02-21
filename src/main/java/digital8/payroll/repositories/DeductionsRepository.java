package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.Deductions;

import java.util.List;

@Repository
public interface DeductionsRepository extends JpaRepository<Deductions, Integer> {
    List<Deductions> findAllByOrderByDeductionNameAsc();
}
