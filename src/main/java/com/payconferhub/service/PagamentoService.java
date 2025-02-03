package com.payconferhub.service;

import com.payconferhub.entities.Pagamento;
import com.payconferhub.entities.PlanoVenda;
import com.payconferhub.repositories.PlanoVendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço responsável pelo cálculo e processamento de pagamentos mensais com base nos planos ativos.
 * Utiliza paradigmas assíncrono, reativo e funcional para otimizar o processamento de dados.
 */
@Service
public class PagamentoService {
    // Logger para registrar eventos e erros no sistema
    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    // Repositório para acessar os planos de venda cadastrados no sistema
    private final PlanoVendaRepository planoVendaRepository;

    // Construtor para injetar o repositório via dependência do Spring
    public PagamentoService(PlanoVendaRepository planoVendaRepository) {
        this.planoVendaRepository = planoVendaRepository;
    }

    /**
     * Calcula o pagamento mensal de forma assíncrona utilizando CompletableFuture.
     *
     * Paradigma Assíncrono: Usa CompletableFuture para não bloquear a execução principal do sistema.
     * Paradigma Reativo: Obtém os planos ativos de forma não bloqueante com reactive streams.
     * Paradigma Funcional: Aplica transformações e reduções nos dados usando streams.
     *
     * @param parceiro Nome do parceiro para o qual será calculado o pagamento.
     * @return CompletableFuture contendo o objeto Pagamento processado.
     */
    public CompletableFuture<Pagamento> calcularPagamentoMensal(String parceiro) {
        logger.info("[Assíncrono] Iniciando cálculo de pagamento para o parceiro: {}", parceiro);

        return planoVendaRepository.findByStatus("ativo") // Busca os planos ativos de forma reativa
                .doOnSubscribe(s -> logger.info("[Reativo] Iniciando a busca de planos ativos"))
                .collectList() // Converte o Flux em uma lista de planos
                .doOnNext(planos -> logger.info("[Reativo] Planos ativos encontrados: {}", planos.size()))
                .toFuture() // Converte para CompletableFuture para continuar o processamento assíncrono
                .thenApply(planos -> {
                    logger.info("[Funcional] Processando lista de planos para calcular o valor total");
                    return planos.stream() // Processa a lista de planos usando programação funcional
                            .map(PlanoVenda::getValor) // Extrai o valor de cada plano (função pura)
                            .reduce(BigDecimal.ZERO, BigDecimal::add); // Soma os valores de todos os planos
                })
                .thenCompose(valorTotal -> CompletableFuture.supplyAsync(() -> {
                    logger.info("[Assíncrono] Valor total calculado: {}", valorTotal);

                    try {
                        logger.info("[Assíncrono] Simulando processamento demorado...");
                        Thread.sleep(5000); // Simulação de um processamento demorado (poderia ser um acesso a banco ou API externa)
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Mantém a thread informada sobre a interrupção
                    }

                    // Criação do objeto Pagamento com os valores calculados
                    Pagamento pagamento = new Pagamento(null, parceiro, valorTotal, LocalDate.now());

                    // Salva os dados do pagamento em um arquivo CSV (simulação de persistência)
                    salvarPagamentoEmArquivo(pagamento);

                    return pagamento; // Retorna o pagamento processado
                }));
    }

    /**
     * Salva os dados do pagamento em um arquivo CSV para registro.
     *
     * @param pagamento Objeto contendo as informações do pagamento a ser registrado.
     */
    private void salvarPagamentoEmArquivo(Pagamento pagamento) {
        String filePath = "pagamentos.csv"; // Caminho do arquivo onde os pagamentos serão registrados
        try (FileWriter writer = new FileWriter(filePath, true)) { // Modo append (não sobrescreve)
            writer.append(pagamento.getParceiro()) // Adiciona o nome do parceiro
                    .append(",")
                    .append(pagamento.getValorTotal().toString()) // Adiciona o valor total do pagamento
                    .append(",")
                    .append(pagamento.getDataPagamento().toString()) // Adiciona a data do pagamento
                    .append("\n"); // Nova linha para o próximo registro
            logger.info("[Persistência] Pagamento salvo no arquivo CSV: {}", filePath);
        } catch (IOException e) {
            logger.error("[Persistência] Erro ao salvar pagamento no arquivo CSV", e);
        }
    }

    /**
     * Metodo que simula uma tarefa paralela enquanto o cálculo do pagamento ocorre.
     *
     * Paradigma Assíncrono: Demonstra execução paralela independente do cálculo principal.
     */
    public void executarOutraTarefa() {
        logger.info("[Paralelo] Executando outra tarefa enquanto o pagamento é calculado...");
        try {
            Thread.sleep(1000); // Simulação de uma operação que leva 1 segundo
        } catch (InterruptedException e) {
            logger.error("[Paralelo] Erro ao executar tarefa paralela", e);
            Thread.currentThread().interrupt(); // Mantém a thread informada sobre a interrupção
        }
        logger.info("[Paralelo] Outra tarefa concluída.");
    }
}