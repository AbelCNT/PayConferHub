package com.payconferhub.controllers;

import com.payconferhub.entities.PlanoVenda;
import com.payconferhub.service.PlanoVendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/planos")
public class PlanoVendaController {
    private final PlanoVendaService planoVendaService;

    public PlanoVendaController(PlanoVendaService planoVendaService) {
        this.planoVendaService = planoVendaService;
    }

    @PostMapping("/upload")
    public Mono<ResponseEntity<Void>> uploadPlanosCSV(@RequestBody Flux<PlanoVenda> planosFlux) {
        planoVendaService.processarPlanosCSV(planosFlux).subscribe();
        return Mono.just(ResponseEntity.ok().build());
    }
}