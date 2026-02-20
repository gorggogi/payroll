package digital8.payroll.controllers;

import digital8.payroll.entities.Positions;
import digital8.payroll.repositories.PositionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/positions")
public class PositionsController {

    @Autowired
    private PositionsRepository positionsRepository;

    @GetMapping
    public ResponseEntity<List<Positions>> getAll() {
        return ResponseEntity.ok(positionsRepository.findAll());
    }
}
