package digital8.payroll.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import digital8.payroll.entities.Departments;
import digital8.payroll.entities.Positions;


import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table (name="employees")
@JsonPropertyOrder({
    "employeeId",
    "employeeNumber",
    "lastName",
    "firstName",
    "middleName",
    "email",
    "contactNumber",
    "department",
    "position",
    "employmentStatus",
    "employmentType",
    "basicSalary",
    "dateHired",
    "birthDate",
    "address",
    "sssNumber",
    "tin",
    "philhealthNumber",
    "pagibigNumber",
    "bank_Account",
    "payType"
})
public class Employees{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column (nullable = false, unique = true)
    private Integer employeeId;

    @Column (nullable = false, unique = true)
    private String employeeNumber;

    @Column (nullable = false, unique = false)
    private String firstName;

    @Column (nullable = false, unique = false)
    private String middleName;

    @Column (nullable = false, unique = false)
    private String lastName;

    @Column (nullable = false, unique = false)
    private LocalDate birthDate;

    @Column (nullable = false, unique = false)
    private String address;

    @Column (nullable = false, unique = true)
    private String email;

    @Column (nullable = false, unique = true)
    private String contactNumber;

    @Column (nullable = false, unique = false)
    private LocalDate dateHired;

    @Column (nullable = false, unique = false)
    private String employmentStatus;
    
    @Column (nullable = false, unique = false)
    private String employmentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departmentId", nullable = false)
    @JsonIgnoreProperties({"employees"})
    private Departments department;

    @Column (nullable = false, unique = false)
    private String payType;

    @Column (nullable = false, unique = false)
    private BigDecimal basicSalary;

    @Column (nullable = false, unique = true)
    private String bank_Account;

    @Column (nullable = false, unique = true)
    private String tin;
    
    @Column (nullable = false, unique = true)
    private String sssNumber;

    @Column (nullable = false, unique = true)
    private String philhealthNumber;

    @Column (nullable = false, unique = true)
    private String pagibigNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "positionId", nullable = false)
    @JsonIgnoreProperties({"employees"})
    private Positions position;

    public Positions getPosition() {
        return position;
    }

    public void setPosition(Positions position) {
        this.position = position;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBank_Account() {
        return bank_Account;
    }

    public void setBank_Account(String bank_Account) {
        this.bank_Account = bank_Account;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDate getDateHired() {
        return dateHired;
    }

    public void setDateHired(LocalDate dateHired) {
        this.dateHired = dateHired;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public Departments getDepartment() {
        return department;
    }

    public void setDepartment(Departments department) {
        this.department = department;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getSssNumber() {
        return sssNumber;
    }

    public void setSssNumber(String sssNumber) {
        this.sssNumber = sssNumber;
    }

    public String getPhilhealthNumber() {
        return philhealthNumber;
    }

    public void setPhilhealthNumber(String philhealthNumber) {
        this.philhealthNumber = philhealthNumber;
    }

    public String getPagibigNumber() {
        return pagibigNumber;
    }

    public void setPagibigNumber(String pagibigNumber) {
        this.pagibigNumber = pagibigNumber;
    }






}



    
    

    

