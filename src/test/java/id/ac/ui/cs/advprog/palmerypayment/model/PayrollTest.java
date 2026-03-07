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
        Payroll payroll = new Payroll(wallet, new BigDecimal("350.00"), "Payroll April");

        assertNull(payroll.getId());
        assertEquals(wallet, payroll.getWallet());
        assertEquals(new BigDecimal("350.00"), payroll.getAmount());
        assertEquals("Payroll April", payroll.getDescription());
        assertNull(payroll.getPaidAt());

        payroll.prePersist();
        Instant paidAt = payroll.getPaidAt();
        assertNotNull(paidAt);

        payroll.prePersist();
        assertEquals(paidAt, payroll.getPaidAt());
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
        assertNull(payroll.getPaidAt());
    }

    @Test
    void prePersistDoesNotOverrideExistingPaidAt() {
        Wallet wallet = new Wallet("user-3", new BigDecimal("100.00"));
        Payroll payroll = new Payroll(wallet, new BigDecimal("25.00"), "Payroll Test");
        Instant fixedPaidAt = Instant.parse("2026-04-01T00:00:00Z");
        ReflectionTestUtils.setField(payroll, "paidAt", fixedPaidAt);

        payroll.prePersist();

        assertEquals(fixedPaidAt, payroll.getPaidAt());
    }
}
