package com.payconferhub.service;

import com.payconferhub.entities.Pagamento;
import com.payconferhub.entities.PlanoVenda;
import com.payconferhub.repositories.PlanoVendaRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de testes para validar as funcionalidades do PagamentoService.
 * Aplica os paradigmas Assíncrono e Reativo.
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
    void deveCalcularPagamentoMensalAssincrono() throws ExecutionException, InterruptedException {
        List<PlanoVenda> planosAtivos = List.of(
                new PlanoVenda(1L, "Ouro", "ativo", new BigDecimal("1000.00"), LocalDate.now()),
                new PlanoVenda(2L, "Bronze", "ativo", new BigDecimal("500.00"), LocalDate.now())
        );

        // Simula a busca de planos ativos como um fluxo reativo
        when(planoVendaRepository.findByStatus("ativo")).thenReturn(Flux.fromIterable(planosAtivos));

        // Executa o cálculo assíncrono
        CompletableFuture<Pagamento> futuroPagamento = pagamentoService.calcularPagamentoMensal("Parceiro1");

        // Executar outra tarefa enquanto aguarda pagamento assíncrono
        pagamentoService.executarOutraTarefa();

        // Obtém o resultado final de forma bloqueante para validação
        Pagamento pagamento = futuroPagamento.get();

        // Validações dos resultados do pagamento
        assertEquals("Parceiro1", pagamento.getParceiro());
        assertEquals(new BigDecimal("1500.00"), pagamento.getValorTotal());

        // Verifica se o metodo reativo do repositório foi chamado
        verify(planoVendaRepository, times(1)).findByStatus("ativo");
    }
}
