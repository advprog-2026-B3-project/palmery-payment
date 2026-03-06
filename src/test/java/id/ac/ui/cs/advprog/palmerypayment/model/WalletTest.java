package id.ac.ui.cs.advprog.palmerypayment.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WalletTest {

    @Test
    void walletConstructorAndBalanceSetterWork() {
        Wallet wallet = new Wallet("user-99", new BigDecimal("40.00"));

        assertNull(wallet.getId());
        assertEquals("user-99", wallet.getUserId());
        assertEquals(new BigDecimal("40.00"), wallet.getBalance());

        wallet.setBalance(new BigDecimal("50.25"));
        assertEquals(new BigDecimal("50.25"), wallet.getBalance());
    }

    @Test
    void defaultConstructorIsAccessibleForJpa() throws Exception {
        Constructor<Wallet> constructor = Wallet.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Wallet wallet = constructor.newInstance();

        assertNotNull(wallet);
        assertNull(wallet.getId());
        assertNull(wallet.getUserId());
        assertNull(wallet.getBalance());
    }
}
