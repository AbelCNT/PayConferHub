package com.payconferhub.controllers;

import com.payconferhub.entities.Pagamento;
import com.payconferhub.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {
    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/{parceiro}")
    public Mono<ResponseEntity<Pagamento>> calcularPagamento(@PathVariable String parceiro) {
        return Mono.fromFuture(pagamentoService.calcularPagamentoMensal(parceiro))
                .map(ResponseEntity::ok);
    }
}
