package digital8.payroll.repositories;

import digital8.payroll.entities.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftsRepository extends JpaRepository<Shift, Integer> {
    List<Shift> findAllByOrderByShiftNameAsc();
}

