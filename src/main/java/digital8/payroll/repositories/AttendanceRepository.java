package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.Attendance;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer>{
	@Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId ORDER BY a.attendance_date DESC")
	List<Attendance> findByEmployeeIdOrderByDateDesc(@Param("employeeId") Integer employeeId);

}


