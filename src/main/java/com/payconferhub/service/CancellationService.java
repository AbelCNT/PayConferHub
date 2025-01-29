package com.payconferhub.service;

import com.payconferhub.model.Cancellation;
import com.payconferhub.repository.CancellationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CancellationService {
    @Autowired
    private CancellationRepository cancellationRepository;

    public List<Cancellation> getAllCancellations() {
        return cancellationRepository.findAll();
    }

    public void saveCancellation(Cancellation cancellation) {
        cancellationRepository.save(cancellation);
    }
}
