package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.Adjustments;
import java.util.List;

@Repository
public interface AdjustmentsRepository extends JpaRepository<Adjustments, Integer> {
    List<Adjustments> findAllByOrderByAdjustmentNameAsc();
}
