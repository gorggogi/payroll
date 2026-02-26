package digital8.payroll.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import digital8.payroll.entities.LeaveRequests;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequests, Integer> {

    List<LeaveRequests> findByEmployee_EmployeeIdOrderByRequestedDateDesc(Integer employeeId);

    List<LeaveRequests> findByStatusOrderByRequestedDateDesc(String status);

    List<LeaveRequests> findAllByOrderByRequestedDateDesc();

    List<LeaveRequests> findByStatusOrderByRequestedDateAsc(String status);

    Long countByStatus(String status);

    @Query("SELECT lr FROM LeaveRequests lr JOIN lr.employee e JOIN lr.leaveType lt " +
            "WHERE (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(lr.reason) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(lt.leaveName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY lr.requestedDate DESC")
    List<LeaveRequests> searchByKeywordOrderByRequestedDateDesc(@Param("search") String search);

    @Query("SELECT lr FROM LeaveRequests lr JOIN lr.employee e JOIN lr.leaveType lt " +
            "WHERE lr.status = :status " +
            "AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(lr.reason) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(lt.leaveName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY lr.requestedDate DESC")
    List<LeaveRequests> searchByKeywordAndStatusOrderByRequestedDateDesc(@Param("search") String search, @Param("status") String status);
}
