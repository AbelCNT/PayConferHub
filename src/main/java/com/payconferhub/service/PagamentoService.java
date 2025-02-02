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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PagamentoService {
    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    private final PlanoVendaRepository planoVendaRepository;

    public PagamentoService(PlanoVendaRepository planoVendaRepository) {
        this.planoVendaRepository = planoVendaRepository;
    }

    // Cálculo assíncrono de pagamentos mensais utilizando CompletableFuture.
    // Paradigma Assíncrono: A utilização de CompletableFuture permite processar os dados sem bloquear a execução principal do programa.
    // Paradigma Funcional: Operamos transformações nos dados usando streams e funções puras.
    public CompletableFuture<Pagamento> calcularPagamentoMensal(String parceiro) {
        logger.info("Iniciando cálculo de pagamento para o parceiro: {}", parceiro);

        return planoVendaRepository.findByStatus("ativo")
                // Paradigma Reativo: Coletamos dados de forma não bloqueante usando reactive streams.
                .collectList()
                .toFuture()
                .thenApply(planos -> planos.stream()
                        .map(PlanoVenda::getValor) // Função pura para extrair valores dos planos
                        .reduce(BigDecimal.ZERO, BigDecimal::add)) // Redução funcional para somar os valores
                .thenCompose(valorTotal -> CompletableFuture.supplyAsync(() -> {
                    logger.info("Valor total calculado: {}", valorTotal);
                    try {
                        Thread.sleep(5000); // Simulação de processamento lento
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Criação do objeto pagamento
                    Pagamento pagamento = new Pagamento(null, parceiro, valorTotal, LocalDate.now());

                    // Simulação de salvamento do pagamento em um arquivo CSV
                    salvarPagamentoEmArquivo(pagamento);

                    return pagamento;
                }));
    }

    // Metodo para salvar os dados do pagamento em um arquivo CSV.
    private void salvarPagamentoEmArquivo(Pagamento pagamento) {
        String filePath = "pagamentos.csv";
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.append(pagamento.getParceiro())
                    .append(",")
                    .append(pagamento.getValorTotal().toString())
                    .append(",")
                    .append(pagamento.getDataPagamento().toString())
                    .append("\n");
            logger.info("Pagamento salvo no arquivo CSV: {}", filePath);
        } catch (IOException e) {
            logger.error("Erro ao salvar pagamento no arquivo CSV", e);
        }
    }

    // Metodo que simula uma tarefa paralela enquanto o cálculo do pagamento ocorre.
    // Paradigma Assíncrono: Demonstra execução paralela independente do cálculo.
    public void executarOutraTarefa() {
        logger.info("Executando outra tarefa enquanto o pagamento é calculado...");
        try {
            Thread.sleep(1000); // Simulação de processamento paralelo
        } catch (InterruptedException e) {
            logger.error("Erro ao executar tarefa paralela", e);
            Thread.currentThread().interrupt();
        }
        logger.info("Outra tarefa concluída.");
    }
}