package com.payconferhub.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class SNSService {

    private final SnsClient snsClient;
    private final String topicArn = "arn:aws:sns:us-east-1:123456789012:PayConferHubNotifications";

    public SNSService() {
        this.snsClient = SnsClient.builder().region(Region.US_EAST_1).build();
    }

    public void enviarNotificacao(String mensagem) {
        PublishRequest request = PublishRequest.builder()
            .message(mensagem)
            .topicArn(topicArn)
            .build();

        snsClient.publish(request);
    }
}
