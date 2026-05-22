package id.ac.ui.cs.advprog.palmerypayment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.palmerypayment.config.PaymentGatewayProperties;
import id.ac.ui.cs.advprog.palmerypayment.dto.MidtransStatusResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;
import java.util.List;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MidtransGatewayClient {

    private final PaymentGatewayProperties paymentGatewayProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MidtransGatewayClient(
            PaymentGatewayProperties paymentGatewayProperties,
            ObjectMapper objectMapper
    ) {
        this.paymentGatewayProperties = paymentGatewayProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public MidtransCreateResult createSnapTransaction(
            String orderId,
            BigDecimal amountRupiah,
            String adminUserId,
            String finishUrl,
            String paymentMethod
    ) {
        ensureConfigured();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("transaction_details", Map.of(
                "order_id", orderId,
                "gross_amount", amountRupiah.setScale(0, RoundingMode.HALF_UP).longValueExact()
        ));
        payload.put("credit_card", Map.of("secure", true));
        payload.put("customer_details", Map.of(
                "first_name", adminUserId,
                "email", adminUserId + "@palmery.local"
        ));
        payload.put("callbacks", Map.of("finish", finishUrl));
        payload.put("enabled_payments", List.of(paymentMethod));

        Map<String, Object> response = postJson(
                paymentGatewayProperties.getMidtrans().getSnapBaseUrl(),
                payload
        );

        String token = stringValue(response.get("token"));
        String redirectUrl = stringValue(response.get("redirect_url"));
        if (token == null || redirectUrl == null) {
            throw new IllegalStateException("Midtrans create transaction response is incomplete");
        }

        return new MidtransCreateResult(token, redirectUrl);
    }

    public MidtransStatusResponse getTransactionStatus(String orderId) {
        ensureConfigured();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(paymentGatewayProperties.getMidtrans().getApiBaseUrl() + "/" + orderId + "/status"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", basicAuthHeader())
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Midtrans status request failed for " + orderId + ": " + response.body());
            }
            return objectMapper.readValue(response.body(), MidtransStatusResponse.class);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch Midtrans transaction status", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to fetch Midtrans transaction status", exception);
        }
    }

    private Map<String, Object> postJson(String url, Map<String, Object> payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", basicAuthHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Midtrans create transaction failed: " + response.body());
            }
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to communicate with Midtrans", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to communicate with Midtrans", exception);
        }
    }

    private String basicAuthHeader() {
        String raw = paymentGatewayProperties.getMidtrans().getServerKey() + ":";
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private void ensureConfigured() {
        if (!paymentGatewayProperties.getMidtrans().isEnabled()) {
            throw new IllegalStateException("Midtrans integration is disabled");
        }
        if (paymentGatewayProperties.getMidtrans().getServerKey() == null
                || paymentGatewayProperties.getMidtrans().getServerKey().isBlank()) {
            throw new IllegalStateException("Midtrans server key is not configured");
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    public record MidtransCreateResult(String token, String redirectUrl) {
    }
}
