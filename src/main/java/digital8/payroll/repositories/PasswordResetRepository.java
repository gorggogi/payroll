package digital8.payroll.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import digital8.payroll.entities.PasswordResetToken;
import digital8.payroll.entities.Users;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, Integer>{
    
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(Users user);

    void deleteByToken(String token);

    // find by token
    // find by user
    // delete by token
    
}
