package digital8.payroll.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.LeaveBalanceId;
import digital8.payroll.entities.LeaveBalance;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, LeaveBalanceId>{
    List<LeaveBalance> findByEmployeeId(Integer employeeId);

    @Transactional
    void deleteByEmployeeId(Integer employeeId);
}
