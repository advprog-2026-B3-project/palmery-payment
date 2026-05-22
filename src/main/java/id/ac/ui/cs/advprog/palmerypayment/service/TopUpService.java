package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.CreateTopUpRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.MidtransNotificationRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.MidtransStatusResponse;
import id.ac.ui.cs.advprog.palmerypayment.dto.TopUpView;
import id.ac.ui.cs.advprog.palmerypayment.model.PaymentTopUp;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PaymentTopUpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class TopUpService {

    private static final BigDecimal RUPIAH_PER_SAWIT_DOLLAR = new BigDecimal("10000");
    private static final Set<String> SUPPORTED_PAYMENT_METHODS = Set.of(
            "gopay",
            "qris",
            "bca_va",
            "bni_va",
            "bri_va",
            "permata_va",
            "echannel",
            "credit_card",
            "shopeepay"
    );

    private final PaymentTopUpRepository paymentTopUpRepository;
    private final WalletService walletService;
    private final MidtransGatewayClient midtransGatewayClient;
    private final id.ac.ui.cs.advprog.palmerypayment.config.PaymentGatewayProperties paymentGatewayProperties;

    public TopUpService(
            PaymentTopUpRepository paymentTopUpRepository,
            WalletService walletService,
            MidtransGatewayClient midtransGatewayClient,
            id.ac.ui.cs.advprog.palmerypayment.config.PaymentGatewayProperties paymentGatewayProperties
    ) {
        this.paymentTopUpRepository = paymentTopUpRepository;
        this.walletService = walletService;
        this.midtransGatewayClient = midtransGatewayClient;
        this.paymentGatewayProperties = paymentGatewayProperties;
    }

    @Transactional
    public TopUpView create(CreateTopUpRequest request, String adminUserId) {
        if (request.getAmountRupiah() == null || request.getAmountRupiah().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amountRupiah must be positive");
        }
        if (request.getAmountRupiah().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("amountRupiah must be a whole Rupiah value");
        }
        if (adminUserId == null || adminUserId.isBlank()) {
            throw new IllegalArgumentException("adminUserId is required");
        }
        String paymentMethod = normalizePaymentMethod(request.getPaymentMethod());

        String reference = "TOPUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal normalizedAmountRupiah = request.getAmountRupiah().setScale(2, RoundingMode.HALF_UP);
        BigDecimal creditedAmount = normalizedAmountRupiah
                .divide(RUPIAH_PER_SAWIT_DOLLAR, 2, RoundingMode.HALF_UP);
        String finishUrl = UriComponentsBuilder.fromUriString(paymentGatewayProperties.getFinishUrl())
                .queryParam("userId", adminUserId)
                .queryParam("topupReference", reference)
                .toUriString();

        MidtransGatewayClient.MidtransCreateResult gatewayResult = midtransGatewayClient.createSnapTransaction(
                reference,
                normalizedAmountRupiah,
                adminUserId,
                finishUrl,
                paymentMethod
        );

        PaymentTopUp topUp = paymentTopUpRepository.save(new PaymentTopUp(
                reference,
                adminUserId,
                normalizedAmountRupiah,
                creditedAmount,
                "PENDING",
                gatewayResult.redirectUrl(),
                gatewayResult.token(),
                "MIDTRANS"
        ));
        topUp.updateGatewayStatus("pending");
        paymentTopUpRepository.save(topUp);

        return toView(topUp);
    }

    @Transactional
    public TopUpView confirm(String reference) {
        return applyGatewayStatus(reference, "settlement", "accept");
    }

    @Transactional(readOnly = true)
    public TopUpView getByReference(String reference, String requesterUserId, boolean admin) {
        return toView(findAccessibleTopUp(reference, requesterUserId, admin));
    }

    @Transactional
    public TopUpView handleMidtransNotification(MidtransNotificationRequest request) {
        if (!isValidMidtransSignature(request)) {
            throw new IllegalArgumentException("invalid Midtrans signature");
        }
        return applyGatewayStatus(
                request.getOrderId(),
                request.getTransactionStatus(),
                request.getFraudStatus()
        );
    }

    @Transactional
    public TopUpView syncStatus(String reference, String requesterUserId, boolean admin) {
        PaymentTopUp topUp = findAccessibleTopUp(reference, requesterUserId, admin);
        MidtransStatusResponse statusResponse = midtransGatewayClient.getTransactionStatus(topUp.getReference());
        return applyGatewayStatus(
                statusResponse.getOrderId(),
                statusResponse.getTransactionStatus(),
                statusResponse.getFraudStatus()
        );
    }

    public boolean isValidMidtransSignature(MidtransNotificationRequest request) {
        if (request.getOrderId() == null || request.getStatusCode() == null || request.getGrossAmount() == null) {
            return false;
        }
        String serverKey = paymentGatewayProperties.getMidtrans().getServerKey();
        if (serverKey == null || serverKey.isBlank() || request.getSignatureKey() == null || request.getSignatureKey().isBlank()) {
            return false;
        }

        String raw = request.getOrderId() + request.getStatusCode() + request.getGrossAmount() + serverKey;
        return sha512Hex(raw).equalsIgnoreCase(request.getSignatureKey());
    }

    private TopUpView applyGatewayStatus(String reference, String transactionStatus, String fraudStatus) {
        PaymentTopUp topUp = paymentTopUpRepository.findByReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("top-up reference not found"));

        String normalizedStatus = transactionStatus == null ? "" : transactionStatus.trim().toLowerCase();
        topUp.updateGatewayStatus(normalizedStatus);

        if (isSuccessfulStatus(normalizedStatus, fraudStatus) && !"PAID".equals(topUp.getStatus())) {
            Wallet adminWallet = walletService.getOrCreateWallet(topUp.getAdminUserId());
            walletService.addBalance(adminWallet, topUp.getCreditedAmount());
            topUp.markPaid(normalizedStatus);
            paymentTopUpRepository.save(topUp);
            return toView(topUp);
        }

        if (isFailedStatus(normalizedStatus)) {
            topUp.markFailed(normalizedStatus);
            paymentTopUpRepository.save(topUp);
            return toView(topUp);
        }

        paymentTopUpRepository.save(topUp);
        return toView(topUp);
    }

    private PaymentTopUp findAccessibleTopUp(String reference, String requesterUserId, boolean admin) {
        PaymentTopUp topUp = paymentTopUpRepository.findByReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("top-up reference not found"));
        if (!admin && !topUp.getAdminUserId().equalsIgnoreCase(requesterUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot access another user's top-up");
        }
        return topUp;
    }

    private boolean isSuccessfulStatus(String transactionStatus, String fraudStatus) {
        if ("settlement".equals(transactionStatus)) {
            return true;
        }
        return "capture".equals(transactionStatus)
                && (fraudStatus == null || fraudStatus.isBlank() || "accept".equalsIgnoreCase(fraudStatus));
    }

    private boolean isFailedStatus(String transactionStatus) {
        return "deny".equals(transactionStatus)
                || "cancel".equals(transactionStatus)
                || "expire".equals(transactionStatus)
                || "failure".equals(transactionStatus);
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("paymentMethod is required");
        }
        String normalizedPaymentMethod = paymentMethod.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_PAYMENT_METHODS.contains(normalizedPaymentMethod)) {
            throw new IllegalArgumentException("unsupported payment method: " + paymentMethod);
        }
        return normalizedPaymentMethod;
    }

    private String sha512Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-512 is not available", exception);
        }
    }

    private TopUpView toView(PaymentTopUp topUp) {
        return new TopUpView(
                topUp.getReference(),
                topUp.getAdminUserId(),
                topUp.getAmountRupiah(),
                topUp.getCreditedAmount(),
                topUp.getStatus(),
                topUp.getGatewayUrl(),
                topUp.getCreatedAt(),
                topUp.getPaidAt()
        );
    }
}
