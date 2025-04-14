package com.example.point.global;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "point")
@Component
public class PointProperties {

    private int maxFreeAmount;

    public int getMaxFreeAmount() {
        return maxFreeAmount;
    }

    public void setMaxFreeAmount(int maxFreeAmount) {
        this.maxFreeAmount = maxFreeAmount;
    }
}
