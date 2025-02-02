package com.payconferhub.repositories;

import com.payconferhub.entities.PlanoVenda;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PlanoVendaRepository extends ReactiveCrudRepository<PlanoVenda, Long> {
    Flux<PlanoVenda> findByStatus(String status);
}
