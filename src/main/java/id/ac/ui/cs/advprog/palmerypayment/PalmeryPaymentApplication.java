package id.ac.ui.cs.advprog.palmerypayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PalmeryPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PalmeryPaymentApplication.class, args);
    }

}
