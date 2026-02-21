package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.PhilhealthTable;
import java.util.List;

@Repository
public interface PhilhealthTableRepository extends JpaRepository<PhilhealthTable, Integer> {
    List<PhilhealthTable> findByEffectiveYearOrderByRangeFromAsc(Integer effectiveYear);
}
