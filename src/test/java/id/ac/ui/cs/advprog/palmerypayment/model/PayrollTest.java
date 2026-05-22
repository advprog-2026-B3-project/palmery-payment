package id.ac.ui.cs.advprog.palmerypayment.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PayrollTest {

    @Test
    void payrollConstructorAndPrePersistWork() {
        Wallet wallet = new Wallet("user-1", new BigDecimal("2000.00"));
        Payroll payroll = new Payroll(
                wallet,
                new BigDecimal("350.00"),
                "Payroll April",
                "BURUH",
                "PENDING",
                new BigDecimal("35.00"),
                new BigDecimal("12.00"),
                new BigDecimal("10.00"),
                "90% x 35.00 Kg x SawitDollar 12.00/Kg"
        );

        assertNull(payroll.getId());
        assertEquals(wallet, payroll.getWallet());
        assertEquals(new BigDecimal("350.00"), payroll.getAmount());
        assertEquals("Payroll April", payroll.getDescription());
        assertNull(payroll.getCreatedAt());

        payroll.prePersist();
        Instant createdAt = payroll.getCreatedAt();
        assertNotNull(createdAt);

        payroll.prePersist();
        assertEquals(createdAt, payroll.getCreatedAt());
    }

    @Test
    void defaultConstructorIsAccessibleForJpa() throws Exception {
        Constructor<Payroll> constructor = Payroll.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Payroll payroll = constructor.newInstance();

        assertNull(payroll.getId());
        assertNull(payroll.getWallet());
        assertNull(payroll.getAmount());
        assertNull(payroll.getDescription());
        assertNull(payroll.getCreatedAt());
    }

    @Test
    void prePersistDoesNotOverrideExistingCreatedAt() {
        Wallet wallet = new Wallet("user-3", new BigDecimal("100.00"));
        Payroll payroll = new Payroll(
                wallet,
                new BigDecimal("25.00"),
                "Payroll Test",
                "SUPIR",
                "PENDING",
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                "90% x 10.00 Kg x SawitDollar 10.00/Kg"
        );
        Instant fixedCreatedAt = Instant.parse("2026-04-01T00:00:00Z");
        ReflectionTestUtils.setField(payroll, "createdAt", fixedCreatedAt);

        payroll.prePersist();

        assertEquals(fixedCreatedAt, payroll.getCreatedAt());
    }

    @Test
    void attachBusinessSourceStoresSourceTypeAndId() {
        Wallet wallet = new Wallet("mandor-1", BigDecimal.ZERO);
        Payroll payroll = new Payroll(
                wallet,
                new BigDecimal("450.00"),
                "Payroll Mandor",
                "MANDOR",
                "PENDING",
                new BigDecimal("50.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                "90% x 50.00 Kg x SawitDollar 10.00/Kg"
        );

        payroll.attachBusinessSource("PENGIRIMAN", "pengiriman-1");

        assertEquals("PENGIRIMAN", payroll.getSourceType());
        assertEquals("pengiriman-1", payroll.getSourceId());
    }
}
