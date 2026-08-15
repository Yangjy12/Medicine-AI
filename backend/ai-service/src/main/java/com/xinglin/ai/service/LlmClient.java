package com.xinglin.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {
    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${xinglin.ai.llm.enabled:false}")
    private boolean enabled;
    @Value("${xinglin.ai.llm.endpoint:}")
    private String endpoint;
    @Value("${xinglin.ai.llm.api-key:}")
    private String apiKey;
    @Value("${xinglin.ai.llm.model:gpt-4o-mini}")
    private String model;
    @Value("${xinglin.ai.llm.timeout-seconds:20}")
    private int timeoutSeconds;

    public LlmClient(ObjectMapper objectMapper, RestTemplateBuilder restTemplateBuilder) {
        this.objectMapper = objectMapper;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public String answer(String systemPrompt, String userPrompt) {
        if (!enabled || !StringUtils.hasText(endpoint) || !StringUtils.hasText(apiKey)) {
            return null;
        }
        try {
            RestTemplate restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(message("system", systemPrompt));
            messages.add(message("user", userPrompt));

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.2);
            body.put("messages", messages);

            String response = restTemplate.postForObject(endpoint, new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || !StringUtils.hasText(content.asText())) {
                log.warn("llm response missing content response={}", response);
                return null;
            }
            return content.asText();
        } catch (Exception ex) {
            log.warn("llm call failed endpoint={} model={} error={}", endpoint, model, ex.getMessage());
            return null;
        }
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> value = new HashMap<>();
        value.put("role", role);
        value.put("content", content);
        return value;
    }
}
