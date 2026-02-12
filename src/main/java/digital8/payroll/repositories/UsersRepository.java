package digital8.payroll.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import digital8.payroll.entities.Users;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Users> findByIsActive(Boolean isActive);

    
}
