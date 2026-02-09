package com.teamexp.learnflowapi.payment.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "toss.payments")
public class TossProps {
    private String secretKey;
    private String confirmUrl;
}
