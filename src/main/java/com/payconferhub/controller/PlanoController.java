package com.payconferhub.controller;

import com.payconferhub.service.ProcessamentoService;
import com.payconferhub.service.SNSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/planos")
public class PlanoController {

    @Autowired
    private ProcessamentoService processamentoService;

    @Autowired
    private SNSService snsService;

    @PostMapping("/upload-ativos")
    public ResponseEntity<String> uploadPlanosAtivos(@RequestParam("file") MultipartFile file) {
        try {
            processamentoService.processarPlanilhaAtivos(file);
            snsService.enviarNotificacao("Planilha de planos ativos foi processada.");
            return ResponseEntity.ok("Arquivo de planos ativos processado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao processar arquivo: " + e.getMessage());
        }
    }

    @GetMapping("/calcular-pagamento/{cnpj}")
    public ResponseEntity<BigDecimal> calcularPagamento(@PathVariable String cnpj) {
        BigDecimal pagamento = processamentoService.calcularPagamento(cnpj);
        snsService.enviarNotificacao("Pagamento calculado para CNPJ: " + cnpj);
        return ResponseEntity.ok(pagamento);
    }
}