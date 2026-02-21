package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.TaxTable;
import java.util.List;

@Repository
public interface TaxTableRepository extends JpaRepository<TaxTable, Integer> {
    List<TaxTable> findByEffectiveYearOrderByCompensationFromAsc(Integer effectiveYear);
}
