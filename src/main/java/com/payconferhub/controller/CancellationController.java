package com.payconferhub.controller;

import com.payconferhub.model.Cancellation;
import com.payconferhub.service.CancellationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cancellations")
public class CancellationController {
    @Autowired
    private CancellationService cancellationService;

    @GetMapping
    public List<Cancellation> getAllCancellations() {
        return cancellationService.getAllCancellations();
    }

    @PostMapping
    public ResponseEntity<Cancellation> createCancellation(@RequestBody Cancellation cancellation) {
        cancellationService.saveCancellation(cancellation);
        return ResponseEntity.ok(cancellation);
    }
}
