package com.payconferhub.service;

import com.payconferhub.entities.Pagamento;
import com.payconferhub.entities.PlanoVenda;
import com.payconferhub.repositories.PlanoVendaRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Classe de testes para validar as funcionalidades do PagamentoService.
 * Aplica os paradigmas Assíncrono, Reativo e Funcional.
 */
class PagamentoServiceTest {

    private final PlanoVendaRepository planoVendaRepository = mock(PlanoVendaRepository.class);
    private final PagamentoService pagamentoService = new PagamentoService(planoVendaRepository);

    /**
     * Testa o cálculo de pagamento mensal.
     * Paradigmas aplicados:
     * - Assíncrono: Uso de CompletableFuture para processamento paralelo.
     * - Funcional: Uso de Streams para somar valores de planos.
     * - Reativo: Obtém dados de forma não bloqueante com Flux.
     */
    @Test
    void deveCalcularPagamentoMensal() {
        List<PlanoVenda> planosAtivos = List.of(
                new PlanoVenda(1L, "Ouro", "ativo", new BigDecimal("1000.00"), LocalDate.now()),
                new PlanoVenda(2L, "Bronze", "ativo", new BigDecimal("500.00"), LocalDate.now())
        );

        // Simula a busca de planos ativos como um fluxo reativo
        when(planoVendaRepository.findByStatus("ativo")).thenReturn(Flux.fromIterable(planosAtivos));

        // Executa o cálculo assíncrono reativo
        Mono<Pagamento> pagamentoMono = pagamentoService.calcularPagamentoMensal("Parceiro1");

        // Verifica o resultado usando StepVerifier
        StepVerifier.create(pagamentoMono)
                .expectNextMatches(pagamento -> {
                    assertEquals("Parceiro1", pagamento.getParceiro());
                    assertEquals(new BigDecimal("1500.00"), pagamento.getValorTotal());
                    assertNotNull(pagamento.getDataPagamento());
                    return true;
                })
                .verifyComplete();

        // Verifica se o metodo reativo do repositório foi chamado
        verify(planoVendaRepository, times(1)).findByStatus("ativo");
    }
}