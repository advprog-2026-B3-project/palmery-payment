package id.ac.ui.cs.advprog.palmerypayment.dto;

public class TopUpWebhookRequest {

    private String reference;
    private String status;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
