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

@Service
public class PlanoVendaService {
    private static final Logger logger = LoggerFactory.getLogger(PlanoVendaService.class);

    private final PlanoVendaRepository planoVendaRepository;

    public PlanoVendaService(PlanoVendaRepository planoVendaRepository) {
        this.planoVendaRepository = planoVendaRepository;
    }

    /**
     * Função pura para calcular a meta com base no tipo de plano.
     */
    public PlanoVenda calcularValorMeta(PlanoVenda plano) {
        BigDecimal valorMeta = switch (plano.getTipoPlano()) {
            case "Bronze" -> plano.getValor().multiply(BigDecimal.valueOf(0.05));
            case "Prata" -> plano.getValor().multiply(BigDecimal.valueOf(0.10));
            case "Ouro" -> plano.getValor().multiply(BigDecimal.valueOf(0.15));
            default -> BigDecimal.ZERO;
        };
        logger.info("Meta calculada para o plano {}: {}", plano.getTipoPlano(), valorMeta);
        return new PlanoVenda(plano.getId(), plano.getTipoPlano(), plano.getStatus(), valorMeta, plano.getDataVenda());
    }

    /**
     * Processa os planos ativos aplicando transformações funcionais e salvando no repositório.
     */
    public Flux<PlanoVenda> processarPlanosCSV(Flux<PlanoVenda> planosFlux) {
        return planosFlux
                .filter(plano -> "ativo".equals(plano.getStatus()))
                .doOnNext(plano -> logger.info("Filtrando plano ativo: {}", plano))
                .map(this::calcularValorMeta)
                .doOnNext(plano -> logger.info("Plano transformado: {}", plano))
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(planoVendaRepository::save)
                .doOnComplete(() -> logger.info("Processamento de planos concluído."))
                .subscribeOn(Schedulers.boundedElastic());
    }
}