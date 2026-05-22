package digital8.payroll.repositories;

import digital8.payroll.entities.OvertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Integer> {

    @Transactional
    void deleteByEmployee_EmployeeId(Integer employeeId);

    List<OvertimeRequest> findByEmployee_EmployeeIdOrderByRequestedAtDesc(Integer employeeId);

    List<OvertimeRequest> findByEmployee_EmployeeIdAndWorkDateBetween(
            Integer employeeId, LocalDate fromInclusive, LocalDate toInclusive);

    List<OvertimeRequest> findByStatusOrderByRequestedAtAsc(String status);

    long countByStatus(String status);
}
