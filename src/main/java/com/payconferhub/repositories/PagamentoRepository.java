package com.payconferhub.repositories;

import com.payconferhub.entities.Pagamento;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PagamentoRepository extends ReactiveCrudRepository<Pagamento, Long> {
}
