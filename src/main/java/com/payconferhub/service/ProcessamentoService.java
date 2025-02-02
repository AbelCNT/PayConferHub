package com.payconferhub.service;

import com.payconferhub.model.Plano;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessamentoService {

    // Método para processar um arquivo CSV
    public List<Plano> processarCsv(String caminhoArquivo) throws IOException {
        return null;
    }

    // Método para calcular pagamentos com base nas metas
    public BigDecimal calcularPagamento(String planos) {
        BigDecimal totalPagamento = BigDecimal.ZERO;

        for (Plano plano : planos) {
            // Exemplo de cálculo baseado na categoria do plano
            switch (plano.getCategoria()) {
                case "Bronze":
                    totalPagamento = totalPagamento.add(plano.getValor().multiply(BigDecimal.valueOf(0.05))); // 5%
                    break;
                case "Prata":
                    totalPagamento = totalPagamento.add(plano.getValor().multiply(BigDecimal.valueOf(0.10))); // 10%
                    break;
                case "Ouro":
                    totalPagamento = totalPagamento.add(plano.getValor().multiply(BigDecimal.valueOf(0.15))); // 15%
                    break;
                default:
                    break;
            }
        }
        return totalPagamento;
    }

    // Método adicional para demonstrar a lógica de gerenciamento de metas
    public String verificarMeta(List<Plano> planos) {
        BigDecimal totalPagamentos = calcularPagamento(planos.toString());
        // Exemplo de verificação de metas
        if (totalPagamentos.compareTo(BigDecimal.valueOf(10000)) > 0) {
            return "Meta alcançada!";
        }
        return "Meta não alcançada!";
    }

    public void processarPlanilhaAtivos(MultipartFile file) {
    }
}