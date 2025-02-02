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

class PagamentoServiceTest {

    private final PlanoVendaRepository planoVendaRepository = mock(PlanoVendaRepository.class);
    private final PagamentoService pagamentoService = new PagamentoService(planoVendaRepository);

    @Test
    void testCalcularPagamentoMensal() throws ExecutionException, InterruptedException, IOException {
        List<PlanoVenda> planosAtivos = List.of(
                new PlanoVenda(1L, "Ouro", "ativo", new BigDecimal("1000.00"), LocalDate.now()),
                new PlanoVenda(2L, "Bronze", "ativo", new BigDecimal("500.00"), LocalDate.now())
        );

        // Mockando repositório para retornar dados simulados de forma reativa
        when(planoVendaRepository.findByStatus("ativo")).thenReturn(Flux.fromIterable(planosAtivos));

        // Chamando a função assíncrona para calcular o pagamento mensal
        CompletableFuture<Pagamento> futuroPagamento = pagamentoService.calcularPagamentoMensal("Parceiro1");

        // Executar outra tarefa enquanto aguarda pagamento assíncrono
        pagamentoService.executarOutraTarefa();

        // Espera pelo resultado final
        Pagamento pagamento = futuroPagamento.get();

        // Validações dos resultados do pagamento
        assertEquals("Parceiro1", pagamento.getParceiro());
        assertEquals(new BigDecimal("1500.00"), pagamento.getValorTotal());

        // Verifica se os repositórios foram corretamente invocados
        verify(planoVendaRepository, times(1)).findByStatus("ativo");
    }
}
