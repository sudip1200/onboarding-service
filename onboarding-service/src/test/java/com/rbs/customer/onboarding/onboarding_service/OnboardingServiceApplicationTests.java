package com.rbs.customer.onboarding.onboarding_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OnboardingServiceApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndFetchOnboarding() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String request = """
                {
                  "fullName":"Alex Jordan",
                  "email":"alex@example.com",
                  "phoneNumber":"9876543210",
                  "countryCode":"IN",
                  "documentId":"DOC998877",
                  "monthlyIncome":5000
                }
                """;

        ResponseEntity<String> createResponse = restTemplate.exchange(
                baseUrl("/api/v1/onboardings"),
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode createdNode = objectMapper.readTree(createResponse.getBody());
        String onboardingId = createdNode.get("onboardingId").asText();
        assertThat(onboardingId).isNotBlank();

        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                baseUrl("/api/v1/onboardings/" + onboardingId),
                String.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode detailsNode = objectMapper.readTree(getResponse.getBody());
        assertThat(detailsNode.get("onboardingId").asText()).isEqualTo(onboardingId);
        assertThat(detailsNode.get("status").asText()).isEqualTo("APPROVED");
    }

    @Test
    void shouldReturnValidationErrorForInvalidRequest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String invalidRequest = """
                {
                  "fullName":"",
                  "email":"invalid-email",
                  "phoneNumber":"123",
                  "countryCode":"",
                  "documentId":"",
                  "monthlyIncome":0
                }
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/onboardings"),
                HttpMethod.POST,
                new HttpEntity<>(invalidRequest, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode errorNode = objectMapper.readTree(response.getBody());
        assertThat(errorNode.get("errorCode").asText()).isEqualTo("VALIDATION_ERROR");
        assertThat(errorNode.get("correlationId").asText()).isNotBlank();
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
