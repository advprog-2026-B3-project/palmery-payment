package id.ac.ui.cs.advprog.palmerypayment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentGatewayProperties {

    private String finishUrl = "http://localhost:3000/wallet";
    private Midtrans midtrans = new Midtrans();

    public String getFinishUrl() {
        return finishUrl;
    }

    public void setFinishUrl(String finishUrl) {
        this.finishUrl = finishUrl;
    }

    public Midtrans getMidtrans() {
        return midtrans;
    }

    public void setMidtrans(Midtrans midtrans) {
        this.midtrans = midtrans;
    }

    public static class Midtrans {
        private boolean enabled = true;
        private String merchantId = "";
        private String clientKey = "";
        private String serverKey = "";
        private String snapBaseUrl = "https://app.sandbox.midtrans.com/snap/v1/transactions";
        private String apiBaseUrl = "https://api.sandbox.midtrans.com/v2";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getClientKey() {
            return clientKey;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }

        public String getServerKey() {
            return serverKey;
        }

        public void setServerKey(String serverKey) {
            this.serverKey = serverKey;
        }

        public String getSnapBaseUrl() {
            return snapBaseUrl;
        }

        public void setSnapBaseUrl(String snapBaseUrl) {
            this.snapBaseUrl = snapBaseUrl;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }
    }
}
