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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Serviço responsável pelo cálculo e processamento de pagamentos mensais com base nos planos ativos.
 * Utiliza paradigmas assíncrono, reativo e funcional para otimizar o processamento de dados.
 */
@Service
public class PagamentoService {
    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);
    private final PlanoVendaRepository planoVendaRepository;

    public PagamentoService(PlanoVendaRepository planoVendaRepository) {
        this.planoVendaRepository = planoVendaRepository;
    }

    /**
     * Calcula o pagamento mensal de forma assíncrona.
     * <p>
     * Paradigma Assíncrono: Não bloqueia a execução principal do sistema.
     * Paradigma Reativo: Processa dados de forma não bloqueante usando Mono.
     * Paradigma Funcional: Usa operações funcionais para transformação e processamento de dados.
     *
     * @param parceiro Nome do parceiro para o qual será calculado o pagamento.
     * @return Mono contendo o objeto Pagamento processado.
     */
    public Mono<Pagamento> calcularPagamentoMensal(String parceiro) {
        logger.info("[Assíncrono] Iniciando cálculo de pagamento para o parceiro: {}", parceiro);

        return planoVendaRepository.findByStatus("ativo")
                .doOnSubscribe(subscription -> logger.info("[Reativo] Iniciando busca dos planos ativos"))
                .collectList() // Converte o Flux em uma lista de planos
                .doOnNext(planos -> logger.info("[Reativo] Planos ativos encontrados: {}", planos.size()))
                .flatMap(planos -> {
                    BigDecimal valorTotal = calcularValorTotal(planos); // Processa a lista de planos usando operações funcionais
                    return executarCalculoDemorado(valorTotal, parceiro); // Executa o cálculo assíncrono do pagamento
                })
                .subscribeOn(Schedulers.boundedElastic()); // Define a scheduler para execução do Mono
    }

    /**
     * Calcula o valor total dos planos de forma funcional.
     *
     * @param planos Lista de planos de venda.
     * @return Valor total somado de todos os planos.
     */
    private BigDecimal calcularValorTotal(List<PlanoVenda> planos) {
        logger.info("[Funcional] Processando lista de planos para calcular o valor total");
        return planos.stream()
                .map(PlanoVenda::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Executa o cálculo demorado do pagamento de forma assíncrona e reativa utilizando Mono.
     *
     * @param valorTotal Valor total dos planos calculado previamente.
     * @param parceiro   Nome do parceiro para o qual será calculado o pagamento.
     * @return Mono contendo o objeto Pagamento processado.
     */
    private Mono<Pagamento> executarCalculoDemorado(BigDecimal valorTotal, String parceiro) {
        return Mono.fromCallable(() -> {
                    logger.info("[Assíncrono] Simulando processamento demorado do cálculo...");
                    executarMultiplasTarefasParalelas(3); // Executa tarefas paralelas simuladas
                    try {
                        Thread.sleep(7000); // Simula uma operação demorada
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    logger.info("[Assíncrono] Valor total calculado: {}", valorTotal);

                    // Criação do objeto Pagamento com os valores calculados
                    Pagamento pagamento = new Pagamento(null, parceiro, valorTotal, LocalDate.now());

                    // Salva os dados do pagamento em um arquivo CSV
                    salvarPagamentoEmArquivo(pagamento);

                    return pagamento;
                })
                .doOnSubscribe(subscription -> logger.info("[Reativo] Iniciando cálculo demorado"))
                .doOnSuccess(pagamento -> logger.info("[Reativo] Cálculo demorado concluído"))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Salva os dados do pagamento em um arquivo CSV.
     *
     * @param pagamento Objeto contendo as informações do pagamento a ser registrado.
     */
    private void salvarPagamentoEmArquivo(Pagamento pagamento) {
        String filePath = "pagamentos.csv";
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.append(pagamento.getParceiro())
                    .append(",")
                    .append(pagamento.getValorTotal().toString())
                    .append(",")
                    .append(pagamento.getDataPagamento().toString())
                    .append("\n");
            logger.info("[Persistência] Pagamento salvo no arquivo CSV: {}", filePath);
        } catch (IOException e) {
            logger.error("[Persistência] Erro ao salvar pagamento no arquivo CSV", e);
        }
    }

    /**
     * Executa múltiplas tarefas paralelas de forma assíncrona enquanto o cálculo do pagamento ocorre.
     *
     * @param numeroDeTarefas Número de tarefas a serem executadas em paralelo.
     */
    private void executarMultiplasTarefasParalelas(int numeroDeTarefas) {
        CompletableFuture<?>[] tarefas = IntStream.rangeClosed(1, numeroDeTarefas)
                .mapToObj(numeroTarefa -> CompletableFuture.runAsync(() -> {
                    logger.info("[Paralelo] Executando tarefa {} enquanto o pagamento é calculado...", numeroTarefa);
                    try {
                        Thread.sleep(1000); // Simulação de uma tarefa paralela
                    } catch (InterruptedException e) {
                        logger.error("[Paralelo] Erro ao executar tarefa paralela {}", numeroTarefa, e);
                        Thread.currentThread().interrupt(); // Mantém a thread informada sobre a interrupção
                    }
                    logger.info("[Paralelo] Tarefa {} concluída.", numeroTarefa);
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(tarefas).join(); // Aguarda a conclusão de todas as tarefas paralelas
    }
}