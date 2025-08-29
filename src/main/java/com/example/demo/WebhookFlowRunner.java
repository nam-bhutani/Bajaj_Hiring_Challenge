package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WebhookFlowRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(WebhookFlowRunner.class);

    private final RestTemplate restTemplate;

    @Value("${demo.name}")
    private String name;

    @Value("${demo.regNo}")
    private String regNo;

    @Value("${demo.email}")
    private String email;

    @Value("${demo.api.baseUrl}")
    private String baseUrl;

    public WebhookFlowRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            logger.info("Starting webhook generation and solution submission process.");
            
            WebhookResponse response = generateWebhook();

            if (response != null && response.getWebhook() != null && response.getAccessToken() != null) {
                logger.info("Webhook successfully generated.");
                String finalQuery = solveSqlProblem();
                
                if (finalQuery != null) {
                    logger.info("SQL query for solution determined.");
                    submitSolution(response.getWebhook(), response.getAccessToken(), finalQuery);
                    logger.info("Solution submitted successfully!");
                } else {
                    logger.error("Failed to determine the SQL query based on regNo.");
                }
            } else {
                logger.error("Failed to get webhook URL or access token. Exiting.");
            }
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
        }
    }

    private WebhookResponse generateWebhook() {
        String url = baseUrl + "/generateWebhook/JAVA";
        WebhookRequest requestBody = new WebhookRequest();
        requestBody.setName(this.name);
        requestBody.setRegNo(this.regNo);
        requestBody.setEmail(this.email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<WebhookRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            return restTemplate.postForObject(url, entity, WebhookResponse.class);
        } catch (Exception e) {
            logger.error("API call to generateWebhook failed: {}", e.getMessage());
            return null;
        }
    }

    private String solveSqlProblem() {
        return "SELECT " +
               "p.AMOUNT AS SALARY, " +
               "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
               "YEAR(CURRENT_DATE()) - YEAR(e.DOB) AS AGE, " +
               "d.DEPARTMENT_NAME " +
               "FROM " +
               "PAYMENTS p " +
               "JOIN " +
               "EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
               "JOIN " +
               "DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
               "WHERE " +
               "DAY(p.PAYMENT_TIME) <> 1 " +
               "ORDER BY " +
               "p.AMOUNT DESC " +
               "LIMIT 1;";
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        SolutionRequest requestBody = new SolutionRequest(finalQuery);
        HttpEntity<SolutionRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForLocation(webhookUrl, entity);
        } catch (Exception e) {
            logger.error("Failed to submit solution to webhook: {}", e.getMessage());
        }
    }
}