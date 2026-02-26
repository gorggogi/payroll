package digital8.payroll.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import digital8.payroll.entities.Roles;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Integer>{
    
}
    

