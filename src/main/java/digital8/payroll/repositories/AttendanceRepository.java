package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId ORDER BY a.attendance_date DESC")
    List<Attendance> findByEmployeeIdOrderByDateDesc(@Param("employeeId") Integer employeeId);

    @Query(value = "SELECT * FROM attendance WHERE employeeId = :eid AND attendance_date = :d LIMIT 1", nativeQuery = true)
    Optional<Attendance> findAttendanceOnDate(@Param("eid") Integer employeeId, @Param("d") LocalDate d);
}


