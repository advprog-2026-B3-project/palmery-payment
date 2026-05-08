package id.ac.ui.cs.advprog.palmerypayment.service;

import id.ac.ui.cs.advprog.palmerypayment.dto.CreateTopUpRequest;
import id.ac.ui.cs.advprog.palmerypayment.dto.TopUpView;
import id.ac.ui.cs.advprog.palmerypayment.model.PaymentTopUp;
import id.ac.ui.cs.advprog.palmerypayment.model.Wallet;
import id.ac.ui.cs.advprog.palmerypayment.repository.PaymentTopUpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class TopUpService {

    private static final BigDecimal RUPIAH_PER_SAWIT_DOLLAR = new BigDecimal("10000");

    private final PaymentTopUpRepository paymentTopUpRepository;
    private final WalletService walletService;

    public TopUpService(PaymentTopUpRepository paymentTopUpRepository, WalletService walletService) {
        this.paymentTopUpRepository = paymentTopUpRepository;
        this.walletService = walletService;
    }

    @Transactional
    public TopUpView create(CreateTopUpRequest request) {
        if (request.getAmountRupiah() == null || request.getAmountRupiah().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amountRupiah must be positive");
        }
        if (request.getAdminUserId() == null || request.getAdminUserId().isBlank()) {
            throw new IllegalArgumentException("adminUserId is required");
        }

        String reference = "TOPUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal creditedAmount = request.getAmountRupiah()
                .divide(RUPIAH_PER_SAWIT_DOLLAR, 2, RoundingMode.HALF_UP);

        PaymentTopUp topUp = paymentTopUpRepository.save(new PaymentTopUp(
                reference,
                request.getAdminUserId(),
                request.getAmountRupiah().setScale(2, RoundingMode.HALF_UP),
                creditedAmount,
                "PENDING",
                "https://sandbox.palmery.local/pay/" + reference
        ));

        return toView(topUp);
    }

    @Transactional
    public TopUpView confirm(String reference) {
        PaymentTopUp topUp = paymentTopUpRepository.findByReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("top-up reference not found"));

        if (!"PAID".equals(topUp.getStatus())) {
            Wallet adminWallet = walletService.getOrCreateWallet(topUp.getAdminUserId());
            walletService.addBalance(adminWallet, topUp.getCreditedAmount());
            topUp.markPaid();
            paymentTopUpRepository.save(topUp);
        }

        return toView(topUp);
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
