package com.payconferhub.service;

import com.payconferhub.model.Plano;
import org.springframework.stereotype.Service;

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
        List<Plano> planos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(","); // Supondo que os dados estão separados por vírgula
                Plano plano = new Plano();
                plano.setId(Long.parseLong(dados[0]));
                plano.setNome(dados[1]);
                plano.setValor(new BigDecimal(dados[2]));
                plano.setCategoria(dados[3]);
                planos.add(plano);
            }
        }
        return planos;
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
}