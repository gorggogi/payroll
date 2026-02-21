package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.PayrollItems;
import java.util.List;

@Repository
public interface PayrollItemsRepository extends JpaRepository<PayrollItems, Integer> {
	List<PayrollItems> findByEmployeeId(Integer employeeId);
}
