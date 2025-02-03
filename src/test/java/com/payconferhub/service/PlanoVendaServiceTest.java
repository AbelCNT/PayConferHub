package com.payconferhub.service;

import com.payconferhub.entities.PlanoVenda;
import com.payconferhub.repositories.PlanoVendaRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Classe de testes para validar as funcionalidades do PlanoVendaService.
 * Aplica os paradigmas Funcional e Reativo.
 */
class PlanoVendaServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(PlanoVendaServiceTest.class);

    private final PlanoVendaRepository planoVendaRepository = mock(PlanoVendaRepository.class);
    private final PlanoVendaService planoVendaService = new PlanoVendaService(planoVendaRepository);

    /**
     * Testa o cálculo da meta de um plano de venda.
     * Paradigma aplicado: Funcional (uso de funções puras para cálculo de valores).
     */
    @Test
    void deveCalcularValorMetaFuncionalmente() {
        logger.info("Iniciando teste de cálculo de valor meta");

        PlanoVenda plano = new PlanoVenda(1L, "Ouro", "ativo", new BigDecimal("1000.00"), LocalDate.now());
        PlanoVenda planoCalculado = planoVendaService.calcularValorMeta(plano);

        // Valida se o cálculo foi aplicado corretamente
        assertEquals(0, planoCalculado.getValor().compareTo(new BigDecimal("150.00")));
        logger.info("Teste de valor meta concluído com sucesso");
    }

    /**
     * Testa o processamento de planos vindos de um arquivo CSV.
     * Paradigmas aplicados:
     * - Reativo: Usa Flux para processar múltiplos planos de forma não bloqueante.
     * - Funcional: Usa transformações e filtros para manipular os dados.
     */
    @Test
    void deveProcessarPlanosCSVDeFormaReativa() {
        logger.info("Iniciando teste de processamento de planos CSV");

        Flux<PlanoVenda> planosFlux = Flux.just(
                new PlanoVenda(1L, "Ouro", "ativo", new BigDecimal("1000.00"), LocalDate.now()),
                new PlanoVenda(2L, "Bronze", "ativo", new BigDecimal("500.00"), LocalDate.now()),
                new PlanoVenda(3L, "Prata", "ativo", new BigDecimal("750.00"), LocalDate.now())
        );

        // Simula o comportamento do repositório ao salvar os planos
        when(planoVendaRepository.save(any(PlanoVenda.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Executa o processamento reativo dos planos
        Flux<PlanoVenda> resultFlux = planoVendaService.processarPlanosCSV(planosFlux);

        // Valida os resultados usando StepVerifier
        StepVerifier.create(resultFlux)
                .expectNextMatches(plano -> plano.getTipoPlano().equals("Ouro") && plano.getValor().compareTo(new BigDecimal("150.00")) == 0)
                .expectNextMatches(plano -> plano.getTipoPlano().equals("Bronze") && plano.getValor().compareTo(new BigDecimal("25.00")) == 0)
                .expectNextMatches(plano -> plano.getTipoPlano().equals("Prata") && plano.getValor().compareTo(new BigDecimal("75.00")) == 0)
                .verifyComplete();

        logger.info("Teste de processamento de planos CSV concluído com sucesso");
    }
}