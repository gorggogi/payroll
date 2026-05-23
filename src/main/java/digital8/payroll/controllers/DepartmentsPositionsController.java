package digital8.payroll.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/departments-positions")
public class DepartmentsPositionsController {

    @GetMapping
    public String showDepartmentsPositionsPage() {
        return "html/departmentsPositions";
    }
}
