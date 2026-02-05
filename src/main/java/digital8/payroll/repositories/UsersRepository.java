package digital8.payroll.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.Users;
import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    
    
}
