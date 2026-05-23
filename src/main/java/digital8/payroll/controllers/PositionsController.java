package digital8.payroll.controllers;

import digital8.payroll.entities.Positions;
import digital8.payroll.repositories.PositionsRepository;
import digital8.payroll.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/positions")
public class PositionsController {

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    public ResponseEntity<List<Positions>> getAll() {
        return ResponseEntity.ok(positionsRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Positions> createPosition(@RequestBody Positions position) {
        return ResponseEntity.ok(positionsRepository.save(position));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePosition(@PathVariable Integer id) {
        if (employeeRepository.existsByPosition_PositionId(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot delete position because there are employees assigned to it.");
        }
        positionsRepository.deleteById(id);
        return ResponseEntity.ok("Position deleted successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePosition(@PathVariable Integer id, @RequestBody Positions updatedPos) {
        return positionsRepository.findById(id).map(pos -> {
            pos.setPositionName(updatedPos.getPositionName());
            positionsRepository.save(pos);
            return ResponseEntity.ok(pos);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
