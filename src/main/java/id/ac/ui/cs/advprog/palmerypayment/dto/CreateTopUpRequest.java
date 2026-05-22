package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;

public class CreateTopUpRequest {

    private BigDecimal amountRupiah;
    private String paymentMethod;

    public BigDecimal getAmountRupiah() {
        return amountRupiah;
    }

    public void setAmountRupiah(BigDecimal amountRupiah) {
        this.amountRupiah = amountRupiah;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
