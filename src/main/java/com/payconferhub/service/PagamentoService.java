package com.payconferhub.service;

import com.payconferhub.entities.Pagamento;
import com.payconferhub.entities.PlanoVenda;
import com.payconferhub.repositories.PlanoVendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
public class PagamentoService {
    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);
    private final PlanoVendaRepository planoVendaRepository;

    public PagamentoService(PlanoVendaRepository planoVendaRepository) {
        this.planoVendaRepository = planoVendaRepository;
    }

    public Mono<Pagamento> calcularPagamentoMensal(String parceiro) {
        logger.info("[{}] [Thread-{}] [PagamentoService] [Entrada] calcularPagamentoMensal para parceiro: {}",
                LocalDateTime.now(), Thread.currentThread().getId(), parceiro);

        return planoVendaRepository.findByStatus("ativo")
                .doOnSubscribe(subscription ->
                        logger.info("[{}] [Thread-{}] [PagamentoService] [Reativo] Iniciando busca dos planos ativos",
                                LocalDateTime.now(), Thread.currentThread().getId()))
                .collectList()
                .doOnNext(planos ->
                        logger.info("[{}] [Thread-{}] [PagamentoService] [Reativo] Planos ativos encontrados: {}",
                                LocalDateTime.now(), Thread.currentThread().getId(), planos.size()))
                .flatMap(planos -> {
                    logger.info("[{}] [Thread-{}] [PagamentoService] Processando lista de planos para calcular o valor total",
                            LocalDateTime.now(), Thread.currentThread().getId());
                    BigDecimal valorTotal = calcularValorTotal(planos);
                    return executarCalculoDemorado(valorTotal, parceiro);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doFinally(signalType ->
                        logger.info("[{}] [Thread-{}] [PagamentoService] [Saída] calcularPagamentoMensal para parceiro: {}",
                                LocalDateTime.now(), Thread.currentThread().getId(), parceiro));
    }

    public BigDecimal calcularValorTotal(List<PlanoVenda> planos) {
        return planos.stream()
                .map(PlanoVenda::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<Pagamento> executarCalculoDemorado(BigDecimal valorTotal, String parceiro) {
        long startTime = System.currentTimeMillis();
        logger.info("[{}] [Thread-{}] [PagamentoService] [Reativo] Iniciando cálculo demorado...",
                LocalDateTime.now(), Thread.currentThread().getId());

        return Mono.fromCallable(() -> {
                    logger.info("[{}] [Thread-{}] [PagamentoService] [Assíncrono] Simulando processamento demorado do cálculo...",
                            LocalDateTime.now(), Thread.currentThread().getId());
                    executarMultiplasTarefasParalelas(3);
                    try {
                        Thread.sleep(7000);
                    } catch (InterruptedException e) {
                        logger.error("[{}] [Thread-{}] [PagamentoService] [Assíncrono] Erro durante a simulação de atraso: {}",
                                LocalDateTime.now(), Thread.currentThread().getId(), e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                    Pagamento pagamento = new Pagamento(null, parceiro, valorTotal, LocalDate.now());
                    logger.info("[{}] [Thread-{}] [PagamentoService] [Assíncrono] Valor total calculado: {}",
                            LocalDateTime.now(), Thread.currentThread().getId(), valorTotal);

                    salvarPagamentoEmArquivo(pagamento);
                    return pagamento;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(pagamento -> {
                    long endTime = System.currentTimeMillis();
                    logger.info("[{}] [Thread-{}] [PagamentoService] [Reativo] Cálculo demorado concluído. Tempo: {}ms",
                            LocalDateTime.now(), Thread.currentThread().getId(), endTime - startTime);
                });
    }

    private void salvarPagamentoEmArquivo(Pagamento pagamento) {
        String filePath = "pagamentos.csv";
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.append(pagamento.getParceiro())
                    .append(",")
                    .append(pagamento.getValorTotal().toString())
                    .append(",")
                    .append(pagamento.getDataPagamento().toString())
                    .append("\n");
            logger.info("[{}] [Thread-{}] [PagamentoService] [Persistência] Pagamento salvo no arquivo CSV: {}",
                    LocalDateTime.now(), Thread.currentThread().getId(), filePath);
        } catch (IOException e) {
            logger.error("[{}] [Thread-{}] [PagamentoService] [Persistência] Erro ao salvar pagamento no arquivo CSV",
                    LocalDateTime.now(), Thread.currentThread().getId(), e);
        }
    }

    private void executarMultiplasTarefasParalelas(int numeroDeTarefas) {
        CompletableFuture<?>[] tarefas = IntStream.rangeClosed(1, numeroDeTarefas)
                .mapToObj(numeroTarefa -> CompletableFuture.runAsync(() -> {
                    logger.info("[{}] [Thread-{}] [PagamentoService] [Paralelo] Executando tarefa {}...",
                            LocalDateTime.now(), Thread.currentThread().getId(), numeroTarefa);
                    long startTime = System.currentTimeMillis();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("[{}] [Thread-{}] [PagamentoService] [Paralelo] Erro ao executar tarefa {}: {}",
                                LocalDateTime.now(), Thread.currentThread().getId(), numeroTarefa, e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                    long endTime = System.currentTimeMillis();
                    logger.info("[{}] [Thread-{}] [PagamentoService] [Paralelo] Tarefa {} concluída. Tempo: {}ms",
                            LocalDateTime.now(), Thread.currentThread().getId(), numeroTarefa, endTime - startTime);
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(tarefas).join();
    }
}