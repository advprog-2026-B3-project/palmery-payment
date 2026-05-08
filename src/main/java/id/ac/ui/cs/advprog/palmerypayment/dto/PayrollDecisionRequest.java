package id.ac.ui.cs.advprog.palmerypayment.dto;

public class PayrollDecisionRequest {

    private String adminUserId;
    private String reason;

    public String getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(String adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
