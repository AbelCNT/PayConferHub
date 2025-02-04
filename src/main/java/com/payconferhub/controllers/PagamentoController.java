package com.payconferhub.controller;

import com.payconferhub.entities.Pagamento;
import com.payconferhub.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para gerenciar o cálculo de pagamentos.
 */
@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    /**
     * Endpoint para calcular o pagamento mensal de um parceiro.
     *
     * @param parceiro Nome do parceiro para o qual será calculado o pagamento.
     * @return Mono contendo a resposta com o objeto Pagamento.
     */
    @GetMapping("/{parceiro}")
    public Mono<ResponseEntity<Pagamento>> calcularPagamento(@PathVariable String parceiro) {
        return pagamentoService.calcularPagamentoMensal(parceiro)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Tratamento para caso não exista o parceiro
    }
}
