package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.math.BigDecimal;

public class CreateTopUpRequest {

    private String adminUserId;
    private BigDecimal amountRupiah;

    public String getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(String adminUserId) {
        this.adminUserId = adminUserId;
    }

    public BigDecimal getAmountRupiah() {
        return amountRupiah;
    }

    public void setAmountRupiah(BigDecimal amountRupiah) {
        this.amountRupiah = amountRupiah;
    }
}
