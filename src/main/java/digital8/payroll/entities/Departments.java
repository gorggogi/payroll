package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table (name="Departments")
public class Departments{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column (name = departmentId, unique = true)

    @Column (name = departmentName, unique = true)

}