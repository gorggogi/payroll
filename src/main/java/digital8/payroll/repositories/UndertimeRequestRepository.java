package digital8.payroll.repositories;

import digital8.payroll.entities.UndertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UndertimeRequestRepository extends JpaRepository<UndertimeRequest, Integer> {

    @Transactional
    void deleteByEmployee_EmployeeId(Integer employeeId);

    List<UndertimeRequest> findByEmployee_EmployeeIdOrderByRequestedAtDesc(Integer employeeId);

    List<UndertimeRequest> findByStatusOrderByRequestedAtAsc(String status);

    long countByStatus(String status);

    List<UndertimeRequest> findByStatusAndEmployee_EmployeeIdNotOrderByRequestedAtAsc(String status, Integer employeeId);

    long countByStatusAndEmployee_EmployeeIdNot(String status, Integer employeeId);

    List<UndertimeRequest> findByStatusOrderByRequestedAtDesc(String status);

    List<UndertimeRequest> findAllByOrderByRequestedAtDesc();

    @Query("SELECT u FROM UndertimeRequest u WHERE " +
           "(LOWER(u.employee.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.employee.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY u.requestedAt DESC")
    List<UndertimeRequest> searchByKeywordOrderByRequestedAtDesc(@Param("keyword") String keyword);

    @Query("SELECT ur FROM UndertimeRequest ur JOIN ur.employee e " +
            "WHERE ur.status = :status " +
            "AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(ur.reason) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY ur.requestedAt DESC")
    List<UndertimeRequest> searchByKeywordAndStatusOrderByRequestedAtDesc(@Param("search") String search, @Param("status") String status);

    @Query("SELECT COUNT(u) FROM UndertimeRequest u WHERE u.status = 'Pending' AND u.employee.employeeId != :empId")
    long countPendingExcludingEmployee(@Param("empId") Integer employeeId);

    @Query("SELECT COUNT(ur) FROM UndertimeRequest ur WHERE ur.employee.employeeId = :employeeId " +
           "AND ur.status IN ('Approved', 'Rejected') AND ur.respondedAt IS NOT NULL " +
           "AND (:since IS NULL OR ur.respondedAt > :since)")
    long countNewRespondedForEmployee(@Param("employeeId") Integer employeeId, @Param("since") java.time.LocalDateTime since);
}
