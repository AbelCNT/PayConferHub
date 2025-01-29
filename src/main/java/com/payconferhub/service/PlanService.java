package com.payconferhub.service;

import com.payconferhub.model.Plan;
import com.payconferhub.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {
    @Autowired
    private PlanRepository planRepository;

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public void savePlan(Plan plan) {
        planRepository.save(plan);
    }
}
