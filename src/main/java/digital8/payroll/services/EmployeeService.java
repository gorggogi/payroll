package digital8.payroll.services;

import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Roles;
import digital8.payroll.entities.Users;
import digital8.payroll.entities.LeaveBalance;
import digital8.payroll.entities.LeaveTypes;
import digital8.payroll.repositories.EmployeeRepository;
import digital8.payroll.repositories.RolesRepository;
import digital8.payroll.repositories.LeaveBalanceRepository;
import digital8.payroll.repositories.LeaveTypesRepository;
import digital8.payroll.specifications.EmployeeSpecifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import digital8.payroll.repositories.DepartmentsRepository;
import digital8.payroll.repositories.PositionsRepository;
import digital8.payroll.repositories.UsersRepository;

import java.util.Optional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import digital8.payroll.dto.EmployeeListDto;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired 
    private UsersRepository usersRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveTypesRepository leaveTypesRepository;

    public List<Employees> filterEmployees(
        String searchQuery,
        Integer departmentId,
        Integer positionId,
        String employmentStatus,
        String employmentType,
        String payType,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        String sortBy,
        String direction) {

 
    if (sortBy == null || sortBy.isEmpty()) {
        sortBy = "lastName";
    }
    if (direction == null || direction.isEmpty()) {
        direction = "asc";
    }

    Sort sort;
    if ("desc".equalsIgnoreCase(direction)) {
        sort = Sort.by(Sort.Direction.DESC, sortBy);
    } else {
        sort = Sort.by(Sort.Direction.ASC, sortBy);
    }

    Specification<Employees> spec = EmployeeSpecifications.filterBy(
        searchQuery,
        departmentId,
        positionId,
        employmentStatus,
        employmentType,
        payType,
        minSalary,
        maxSalary
    );

    List<Employees> results = employeeRepository.findAllWithFetch(spec, sort);
    
    return results;
    }

    /** Maps an entity to the lightweight DTO used for the employee list (smaller JSON, faster load). */
    public static EmployeeListDto toListDto(Employees e) {
        EmployeeListDto dto = new EmployeeListDto();
        dto.setEmployeeId(e.getEmployeeId());
        dto.setEmployeeNumber(e.getEmployeeNumber());
        dto.setBiometricId(e.getBiometricId());
        dto.setFirstName(e.getFirstName());
        dto.setMiddleName(e.getMiddleName());
        dto.setLastName(e.getLastName());
        dto.setEmail(e.getEmail());
        dto.setContactNumber(e.getContactNumber());
        dto.setEmploymentStatus(e.getEmploymentStatus());
        dto.setEmploymentType(e.getEmploymentType());
        dto.setPayType(e.getPayType());
        dto.setDateHired(e.getDateHired());
        dto.setBasicSalary(e.getBasicSalary());
        dto.setAllowance(e.getAllowance());
        if (e.getDepartment() != null) dto.setDepartmentName(e.getDepartment().getDepartmentName());
        if (e.getPosition() != null) dto.setPositionName(e.getPosition().getPositionName());
        dto.setOtMultiplier(e.getOtMultiplier());
        return dto;
    }

    /** Returns filtered/sorted employees as list DTOs (lighter payload for list page). */
    public List<EmployeeListDto> filterEmployeesAsListDto(
            String searchQuery,
            Integer departmentId,
            Integer positionId,
            String employmentStatus,
            String employmentType,
            String payType,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String sortBy,
            String direction) {
        List<Employees> list = filterEmployees(searchQuery, departmentId, positionId,
                employmentStatus, employmentType, payType, minSalary, maxSalary, sortBy, direction);
        return list.stream().map(EmployeeService::toListDto).collect(Collectors.toList());
    }

    public Optional<Employees> getEmployeeById(Integer id) {
        return employeeRepository.findById(id);
    }

    @Transactional
    public Optional<Employees> createEmployee(Employees emp, String email) {
        if (email != null && usersRepository.existsByEmail(email)) {
            return Optional.empty();
        }
        if (emp.getEmployeeNumber() == null || emp.getEmployeeNumber().isBlank()) {
            emp.setEmployeeNumber(generateEmployeeNumber());
        } else if (employeeRepository.existsByEmployeeNumber(emp.getEmployeeNumber())) {
            return Optional.empty();
        }
        if (emp.getBiometricId() != null && !emp.getBiometricId().isBlank()) {
            emp.setBiometricId(emp.getBiometricId().trim());
            if (employeeRepository.existsByBiometricId(emp.getBiometricId())) {
                return Optional.empty();
            }
        } else {
            emp.setBiometricId(null);
        }
        if (emp.getDepartment() != null && emp.getDepartment().getDepartmentId() != null) {
            departmentsRepository.findById(emp.getDepartment().getDepartmentId()).ifPresent(emp::setDepartment);
        }
        if (emp.getPosition() != null && emp.getPosition().getPositionId() != null) {
            positionsRepository.findById(emp.getPosition().getPositionId()).ifPresent(emp::setPosition);
        }

        if (emp.getFactorRate() == null || emp.getFactorRate().compareTo(BigDecimal.ZERO) <= 0) {
            emp.setFactorRate(new java.math.BigDecimal("20"));
        }
        if (emp.getAllowance() == null || emp.getAllowance().compareTo(BigDecimal.ZERO) < 0) {
            emp.setAllowance(BigDecimal.ZERO);
        }
        if (emp.getOtMultiplier() == null || emp.getOtMultiplier().compareTo(BigDecimal.ZERO) <= 0) {
            emp.setOtMultiplier(new java.math.BigDecimal("1.0"));
        }
        Employees saved = employeeRepository.save(emp);

        String defaultPassword = saved.getLastName().replace(" ", "") + "123";
        Users user = new Users();
        user.setEmployee(saved);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        Roles employeeRole = rolesRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Employee role not found"));
        user.setRole(employeeRole);
        user.setIsActive(true);
        usersRepository.save(user);

        // Initialize leave balances for this employee for all leave types
        List<LeaveTypes> leaveTypes = leaveTypesRepository.findAll();
        for (LeaveTypes type : leaveTypes) {
            LeaveBalance balance = new LeaveBalance();
            balance.setEmployeeId(saved.getEmployeeId());
            balance.setLeaveTypeId(type.getLeaveTypeId());
            balance.setBalance(new BigDecimal("15"));
            leaveBalanceRepository.save(balance);
        }

        return Optional.of(saved);
    }

    private String generateEmployeeNumber() {
        return employeeRepository.findFirstByOrderByEmployeeIdDesc()
                .map(e -> "EMP" + String.format("%05d", e.getEmployeeId() + 1))
                .orElse("EMP00001");
    }

    public Optional<Employees> updateEmployee(Integer id, Employees updated) {
        Optional<Employees> existingOpt = employeeRepository.findById(id);
        if (!existingOpt.isPresent()) {
            return Optional.empty();
        }
        Employees existing = existingOpt.get();

        // Update basic fields
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getMiddleName() != null) existing.setMiddleName(updated.getMiddleName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getEmail() != null) {
            Optional<Users> userOpt = usersRepository.findByEmployee_EmployeeId(id);
            if(userOpt.isPresent()) {
                Users user = userOpt.get();
                if(!user.getEmail().equals(updated.getEmail())){
                    user.setEmail(updated.getEmail());
                    usersRepository.save(user);
                }
                // Update user role based on isAdmin field if present
                if (updated.getIsAdmin() != null) {
                    boolean isAdmin = Boolean.TRUE.equals(updated.getIsAdmin());
                    int roleId = isAdmin ? 1 : 2; // 1 = ADMIN, 2 = EMPLOYEE
                    Roles newRole = rolesRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
                    if (user.getRole() == null || !user.getRole().getRoleId().equals(roleId)) {
                        user.setRole(newRole);
                        usersRepository.save(user);
                    }
                }
            }
        }
        if (updated.getEmployeeNumber() != null) existing.setEmployeeNumber(updated.getEmployeeNumber());
        if (updated.getBiometricId() != null) {
            String candidate = updated.getBiometricId().trim();
            if (candidate.isEmpty()) {
                existing.setBiometricId(null);
            } else if (!candidate.equals(existing.getBiometricId())) {
                Optional<Employees> owner = employeeRepository.findByBiometricId(candidate);
                if (owner.isPresent() && !owner.get().getEmployeeId().equals(id)) {
                    return Optional.empty();
                }
                existing.setBiometricId(candidate);
            }
        }
        if (updated.getContactNumber() != null) existing.setContactNumber(updated.getContactNumber());
        if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
        if (updated.getDateHired() != null) existing.setDateHired(updated.getDateHired());
        if (updated.getEmploymentStatus() != null) existing.setEmploymentStatus(updated.getEmploymentStatus());
        if (updated.getEmploymentType() != null) existing.setEmploymentType(updated.getEmploymentType());
        if (updated.getPayType() != null) existing.setPayType(updated.getPayType());
        if (updated.getBasicSalary() != null) existing.setBasicSalary(updated.getBasicSalary());
        if (updated.getAllowance() != null && updated.getAllowance().compareTo(BigDecimal.ZERO) >= 0) {
            existing.setAllowance(updated.getAllowance());
        }
        if (updated.getFactorRate() != null && updated.getFactorRate().compareTo(BigDecimal.ZERO) > 0) {
            existing.setFactorRate(updated.getFactorRate());
        }
        if (updated.getTin() != null) existing.setTin(updated.getTin());
        if (updated.getSssNumber() != null) existing.setSssNumber(updated.getSssNumber());
        if (updated.getPhilhealthNumber() != null) existing.setPhilhealthNumber(updated.getPhilhealthNumber());
        if (updated.getPagibigNumber() != null) existing.setPagibigNumber(updated.getPagibigNumber());
        if (updated.getBank_Account() != null) existing.setBank_Account(updated.getBank_Account());
        existing.setHolidayPayEligible(updated.isHolidayPayEligible());
        if (updated.getOtMultiplier() != null && updated.getOtMultiplier().compareTo(BigDecimal.ZERO) > 0) {
            existing.setOtMultiplier(updated.getOtMultiplier());
        }

        // Update department if provided
        if (updated.getDepartment() != null && updated.getDepartment().getDepartmentId() != null) {
            departmentsRepository.findById(updated.getDepartment().getDepartmentId()).ifPresent(existing::setDepartment);
        }

        // Update position if provided
        if (updated.getPosition() != null && updated.getPosition().getPositionId() != null) {
            positionsRepository.findById(updated.getPosition().getPositionId()).ifPresent(existing::setPosition);
        }

        Employees saved = employeeRepository.save(existing);
        return Optional.of(saved);
    }

    public boolean isEmployeeNumberTakenByOther(String employeeNumber, Integer excludeEmployeeId) {
        if (employeeNumber == null || employeeNumber.isBlank()) return false;
        Employees other = employeeRepository.findByEmployeeNumber(employeeNumber.trim());
        return other != null && !other.getEmployeeId().equals(excludeEmployeeId);
    }

    // @Transactional
    // public int createMissingUserAccounts() {
    //     Roles employeeRole = rolesRepository.findById(2)
    //             .orElseThrow(() -> new RuntimeException("Employee role not found"));

    //     List<Employees> allEmployees = employeeRepository.findAll();
    //     int created = 0;

    //     for (Employees emp : allEmployees) {
    //         boolean hasAccount = usersRepository.findByEmployee_EmployeeId(emp.getEmployeeId()).isPresent();
    //         if (hasAccount) continue;

    //         String firstName = emp.getFirstName().replaceAll("\\s+", "").toLowerCase();
    //         String lastName = emp.getLastName().replaceAll("\\s+", "").toLowerCase();
    //         String email = firstName + "." + lastName + "@company.com";

    //         int suffix = 1;
    //         while (usersRepository.existsByEmail(email)) {
    //             email = firstName + "." + lastName + suffix + "@company.com";
    //             suffix++;
    //         }

    //         String defaultPassword = emp.getLastName().replace(" ", "") + "123";

    //         Users user = new Users();
    //         user.setEmployee(emp);
    //         user.setEmail(email);
    //         user.setPasswordHash(passwordEncoder.encode(defaultPassword));
    //         user.setRole(employeeRole);
    //         user.setIsActive(true);
    //         usersRepository.save(user);
    //         created++;
    //     }

    //     return created;
    // }
}


