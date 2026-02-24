package digital8.payroll.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import digital8.payroll.entities.LeaveRequests;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequests, Integer> {

    List<LeaveRequests> findByEmployee_EmployeeIdOrderByRequestedDateDesc(Integer employeeId);
    
    List<LeaveRequests> findByStatusOrderByRequestedDateDesc(String status);

    List<LeaveRequests> findAllByOrderByRequestedDateDesc();

    List<LeaveRequests> findByStatusOrderByRequestedDateAsc(String status);

    Long countByStatus (String status);
}
