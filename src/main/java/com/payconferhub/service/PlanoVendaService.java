package com.payconferhub.service;

import com.payconferhub.entities.PlanoVenda;
import com.payconferhub.repositories.PlanoVendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Serviço responsável pelo processamento dos planos de venda.
 * Utiliza paradigmas funcionais e reativos para otimizar o processamento.
 */
@Service
public class PlanoVendaService {
    private static final Logger logger = LoggerFactory.getLogger(PlanoVendaService.class);

    private final PlanoVendaRepository planoVendaRepository;

    public PlanoVendaService(PlanoVendaRepository planoVendaRepository) {
        this.planoVendaRepository = planoVendaRepository;
    }

    /**
     * Calcula a meta de valor com base no tipo de plano.
     * Paradigma funcional: função pura que não altera o estado externo e sempre retorna o mesmo resultado para os mesmos inputs.
     */
    public PlanoVenda calcularValorMeta(PlanoVenda plano) {
        BigDecimal valorMeta = switch (plano.getTipoPlano()) {
            case "Bronze" -> plano.getValor().multiply(BigDecimal.valueOf(0.05));
            case "Prata" -> plano.getValor().multiply(BigDecimal.valueOf(0.10));
            case "Ouro" -> plano.getValor().multiply(BigDecimal.valueOf(0.15));
            default -> BigDecimal.ZERO;
        };
        logger.info("[Funcional] Meta calculada para o plano {}: {}", plano.getTipoPlano(), valorMeta);
        return new PlanoVenda(plano.getId(), plano.getTipoPlano(), plano.getStatus(), valorMeta, plano.getDataVenda());
    }

    /**
     * Processa os planos de forma assíncrona e reativa.
     * - Filtra apenas os planos ativos.
     * - Aplica a transformação funcional para calcular a meta de valor.
     * - Insere um delay para simular um processamento assíncrono.
     * - Salva os planos processados no repositório de forma reativa.
     */
    public Flux<PlanoVenda> processarPlanosCSV(Flux<PlanoVenda> planosFlux) {
        return planosFlux
                .filter(plano -> "ativo".equals(plano.getStatus())) // Filtrando planos ativos
                .doOnNext(plano -> logger.info("[Reativo] Filtrando plano ativo: {}", plano))
                .map(this::calcularValorMeta) // Transformação funcional para calcular valor meta
                .doOnNext(plano -> logger.info("[Funcional] Plano transformado com meta calculada: {}", plano))
                .delayElements(Duration.ofSeconds(1)) // Simulação de processamento assíncrono
                .doOnNext(plano -> logger.info("[Reativo] Simulando processamento assíncrono para o plano: {}", plano))
                .flatMap(planoVendaRepository::save)  // Reatividade ao salvar no repositório
                .doOnNext(plano -> logger.info("[Reativo] Plano salvo no repositório: {}", plano))
                .doOnComplete(() -> logger.info("[Reativo] Processamento de todos os planos concluído."))
                .subscribeOn(Schedulers.boundedElastic()); // Executa em uma thread elástica para evitar bloqueios
    }
}