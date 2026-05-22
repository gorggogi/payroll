package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import digital8.payroll.entities.PayrollItems;
import java.util.List;

@Repository
public interface PayrollItemsRepository extends JpaRepository<PayrollItems, Integer> {

    @Transactional
    void deleteByEmployeeId(Integer employeeId);

    List<PayrollItems> findByEmployeeId(Integer employeeId);
    List<PayrollItems> findByEmployeeIdOrderByPayrollItemIdDesc(Integer employeeId);
}
