package com.payconferhub;

import com.payconferhub.model.Plan;
import com.payconferhub.repository.PlanRepository;
import com.payconferhub.service.PlanService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

class PlanServiceTest {
    @InjectMocks
    private PlanService planService;

    @Mock
    private PlanRepository planRepository;

    public PlanServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSavePlan() {
        Plan plan = new Plan();
        planService.savePlan(plan);
        verify(planRepository).save(plan);
    }
}
