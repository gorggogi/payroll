package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import digital8.payroll.entities.Employees;
import digital8.payroll.entities.Departments;
import digital8.payroll.entities.Positions;
import digital8.payroll.services.EmployeeService;
import digital8.payroll.repositories.DepartmentsRepository;
import digital8.payroll.repositories.PositionsRepository;
import digital8.payroll.repositories.RolesRepository;
import digital8.payroll.repositories.UsersRepository;
import digital8.payroll.entities.Users;
import digital8.payroll.entities.Roles;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/admin/employees")
public class EmployeeViewController {

    @Autowired
    private EmployeeService employeeService;
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

    @GetMapping("/add")
    public String addEmployeeForm(Model model) {
        List<Departments> departments = departmentsRepository.findAll();
        List<Positions> positions = positionsRepository.findAll();
        model.addAttribute("departments", departments);
        model.addAttribute("positions", positions);
        return "html/addEmployee";
    }

    @PostMapping("/add")
    public String addEmployeeSubmit(
            @RequestParam String firstName,
            @RequestParam String middleName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String contactNumber,
            @RequestParam String address,
            @RequestParam String birthDate,
            @RequestParam String dateHired,
            @RequestParam Integer departmentId,
            @RequestParam Integer positionId,
            @RequestParam String employmentStatus,
            @RequestParam String employmentType,
            @RequestParam String payType,
            @RequestParam java.math.BigDecimal basicSalary,
            @RequestParam String bank_Account,
            @RequestParam String tin,
            @RequestParam String sssNumber,
            @RequestParam String philhealthNumber,
            @RequestParam String pagibigNumber,
            RedirectAttributes redirectAttributes) {

        Employees emp = new Employees();
        emp.setFirstName(firstName);
        emp.setMiddleName(middleName);
        emp.setLastName(lastName);
        emp.setEmail(email);
        emp.setContactNumber(contactNumber);
        emp.setAddress(address);
        emp.setBirthDate(LocalDate.parse(birthDate));
        emp.setDateHired(LocalDate.parse(dateHired));
        emp.setEmploymentStatus(employmentStatus);
        emp.setEmploymentType(employmentType);
        emp.setPayType(payType);
        emp.setBasicSalary(basicSalary);
        emp.setBank_Account(bank_Account);
        emp.setTin(tin);
        emp.setSssNumber(sssNumber);
        emp.setPhilhealthNumber(philhealthNumber);
        emp.setPagibigNumber(pagibigNumber);

        Departments dept = new Departments();
        dept.setDepartmentId(departmentId);
        emp.setDepartment(dept);
        Positions pos = new Positions();
        pos.setPositionId(positionId);
        emp.setPosition(pos);

        java.util.Optional<Employees> savedEmpOpt = employeeService.createEmployee(emp);
        if (savedEmpOpt.isEmpty()){
        redirectAttributes.addFlashAttribute("error", "Could not add employee. Email or employee number may already exist");
        return "redirect:/admin/employees/add";
    }

        Employees savedEmp = savedEmpOpt.get();
        try {
            String cleanLastName = savedEmp.getLastName().replace(" ", "");
            String defaultPassword = cleanLastName + "123";
            Users user = new Users();
            user.setEmployee(savedEmp);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(defaultPassword));

            Roles employeeRole = rolesRepository.findById(2)
            .orElseThrow(() -> new RuntimeException("Employee role not found"));

            user.setRole(employeeRole);
            user.setIsActive(true);
            usersRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Employee added successfully! Login " + email + " / Password: " + defaultPassword);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Employee added but account creation failed!" + e.getMessage());
        }

        return "redirect:/admin/employees";

    }
}
