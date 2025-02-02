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

class PlanoVendaServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(PlanoVendaServiceTest.class);

    private final PlanoVendaRepository planoVendaRepository = mock(PlanoVendaRepository.class);
    private final PlanoVendaService planoVendaService = new PlanoVendaService(planoVendaRepository);

    @Test
    void testCalcularValorMeta() {
        logger.info("Iniciando teste de cálculo de valor meta");

        PlanoVenda plano = new PlanoVenda(1L, "Ouro", "ativo", new BigDecimal("1000.00"), LocalDate.now());
        PlanoVenda planoCalculado = planoVendaService.calcularValorMeta(plano);

        assertEquals(0, planoCalculado.getValor().compareTo(new BigDecimal("150.00")));
        logger.info("Teste de valor meta concluído com sucesso");
    }

    @Test
    void testProcessarPlanosCSV() {
        logger.info("Iniciando teste de processamento de planos CSV");

        Flux<PlanoVenda> planosFlux = Flux.just(
                new PlanoVenda(1L, "Ouro", "ativo", new BigDecimal("1000.00"), LocalDate.now()),
                new PlanoVenda(2L, "Bronze", "ativo", new BigDecimal("500.00"), LocalDate.now()),
                new PlanoVenda(3L, "Prata", "ativo", new BigDecimal("750.00"), LocalDate.now())
        );

        when(planoVendaRepository.save(any(PlanoVenda.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Flux<PlanoVenda> resultFlux = planoVendaService.processarPlanosCSV(planosFlux);

        StepVerifier.create(resultFlux)
                .expectNextMatches(plano -> plano.getTipoPlano().equals("Ouro") && plano.getValor().compareTo(new BigDecimal("150.00")) == 0)
                .expectNextMatches(plano -> plano.getTipoPlano().equals("Bronze") && plano.getValor().compareTo(new BigDecimal("25.00")) == 0)
                .expectNextMatches(plano -> plano.getTipoPlano().equals("Prata") && plano.getValor().compareTo(new BigDecimal("75.00")) == 0)
                .verifyComplete();

        logger.info("Teste de processamento de planos CSV concluído");
    }
}