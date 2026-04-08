package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.PagibigTable;
import java.util.List;

@Repository
public interface PagibigTableRepository extends JpaRepository<PagibigTable, Integer> {
    List<PagibigTable> findByEffectiveYearOrderByRangeFromAsc(Integer effectiveYear);
}
