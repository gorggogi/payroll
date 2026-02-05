package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table (name="Employees")
public class Employees{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column (nullable = false, unique = true)
    private Integer employeeId;

    @Column (nullable = false, unique = true)
    private Integer employeeNumber;

    @Column (nullable = false, unique = false)
    private String firstName;

    @Column (nullable = false, unique = false)
    private String middleName;

    @Column (nullable = false, unique = false)
    private String lastName;

    @Column (nullable = false, unique = false)
    private String birthDate;

    @Column (nullable = false, unique = false)
    private String sex;

    @Column (nullable = false, unique = false)
    private String civilStatus;
    
    @Column (nullable = false, unique = true)
    private String email;

    @Column (nullable = false, unique = true)
    private Integer contactNumber;

    @Column (nullable = false, unique = true)
    private

}



    
    

    

