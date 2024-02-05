package com.kapeta.spring.rabbitmq.types;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HeaderBindings {
    private boolean matchAll;
    private Map<String, Object> headers = new HashMap<>();
}
