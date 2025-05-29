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
import java.time.LocalDateTime;

@Service
public class PlanoVendaService {
    private static final Logger logger = LoggerFactory.getLogger(PlanoVendaService.class);
    private final PlanoVendaRepository planoVendaRepository;

    public PlanoVendaService(PlanoVendaRepository planoVendaRepository) {
        this.planoVendaRepository = planoVendaRepository;
    }

    public PlanoVenda calcularValorMeta(PlanoVenda plano) {
        logger.info("[{}] [Thread-{}] [PlanoVendaService] Calculando meta para o plano: {}",
                LocalDateTime.now(), Thread.currentThread().getId(), plano);

        BigDecimal valorMeta = switch (plano.getTipoPlano()) {
            case "Bronze" -> plano.getValor().multiply(BigDecimal.valueOf(0.05));
            case "Prata" -> plano.getValor().multiply(BigDecimal.valueOf(0.10));
            case "Ouro" -> plano.getValor().multiply(BigDecimal.valueOf(0.15));
            default -> BigDecimal.ZERO;
        };

        PlanoVenda planoComMeta = new PlanoVenda(plano.getId(), plano.getTipoPlano(), plano.getStatus(), valorMeta, plano.getDataVenda());

        logger.info("[{}] [Thread-{}] [PlanoVendaService] Meta calculada para o plano {}: {}",
                LocalDateTime.now(), Thread.currentThread().getId(), plano.getTipoPlano(), valorMeta);
        return planoComMeta;
    }

    public Flux<PlanoVenda> processarPlanosCSV(Flux<PlanoVenda> planosFlux) {
        logger.info("[{}] [Thread-{}] [PlanoVendaService] [Entrada] processarPlanosCSV com flux: {}",
                LocalDateTime.now(), Thread.currentThread().getId(), planosFlux);

        // Flux reativo que representa o stream de planos a serem processados
        return planosFlux
                // Operação reativa de filtro: processa apenas planos ativos
                .filter(plano -> {
                    boolean ativo = "ativo".equals(plano.getStatus());
                    logger.info("[{}] [Thread-{}] [PlanoVendaService] [Reativo] Filtrando plano: {}, Ativo: {}",
                            LocalDateTime.now(), Thread.currentThread().getId(), plano, ativo);
                    return ativo;
                })
                // Operação reativa de transformação: calcula o valor da meta para cada plano
                .map(this::calcularValorMeta)
                .doOnNext(plano ->
                        logger.info("[{}] [Thread-{}] [PlanoVendaService] Plano transformado com meta calculada: {}",
                                LocalDateTime.now(), Thread.currentThread().getId(), plano))
                // Operação reativa que introduz um atraso em cada item do fluxo (simulando um processamento assíncrono individual dentro do fluxo reativo)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(plano ->
                        logger.info("[{}] [Thread-{}] [PlanoVendaService] [Reativo] Simulando processamento assíncrono para o plano: {}",
                                LocalDateTime.now(), Thread.currentThread().getId(), plano))
                // Operação reativa que salva cada plano processado no repositório (a operação de save pode ser assíncrona internamente)
                .flatMap(planoVendaRepository::save)
                .doOnNext(plano ->
                        logger.info("[{}] [Thread-{}] [PlanoVendaService] [Reativo] Plano salvo no repositório: {}",
                                LocalDateTime.now(), Thread.currentThread().getId(), plano))
                .doOnComplete(() ->
                        logger.info("[{}] [Thread-{}] [PlanoVendaService] [Reativo] Processamento de todos os planos concluído.",
                                LocalDateTime.now(), Thread.currentThread().getId()))
                // Executa as operações do fluxo em um scheduler diferente para não bloquear a thread principal (comportamento reativo)
                .subscribeOn(Schedulers.boundedElastic())
                // Ação a ser executada quando o fluxo termina (com sucesso, erro ou cancelamento)
                .doFinally(signalType ->
                        logger.info("[{}] [Thread-{}] [PlanoVendaService] [Saída] processarPlanosCSV com signal: {}",
                                LocalDateTime.now(), Thread.currentThread().getId(), signalType));
    }
}