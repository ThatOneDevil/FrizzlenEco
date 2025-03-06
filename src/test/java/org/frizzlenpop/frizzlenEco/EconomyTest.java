package org.frizzlenpop.frizzlenEco;

import org.frizzlenpop.frizzlenEco.economy.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple tests for the economy system
 */
public class EconomyTest {
    
    @Test
    public void testCurrencyFormatting() {
        Currency currency = Currency.builder()
                .id("test")
                .name("Test Currency")
                .symbol("$")
                .format("%s%s")
                .decimalPlaces(2)
                .build();
        
        assertEquals("$10.00", currency.format(new BigDecimal("10")));
        assertEquals("$10.50", currency.format(new BigDecimal("10.5")));
        assertEquals("$10.55", currency.format(new BigDecimal("10.55")));
        assertEquals("$10.56", currency.format(new BigDecimal("10.555")));
    }
    
    @Test
    public void testCurrencyBuilder() {
        Currency currency = Currency.builder()
                .id("test")
                .name("Test Currency")
                .symbol("$")
                .format("%s%s")
                .decimalPlaces(2)
                .isDefault(true)
                .initialBalance(BigDecimal.valueOf(100))
                .minBalance(BigDecimal.ZERO)
                .maxBalance(BigDecimal.valueOf(1000000))
                .interestRate(BigDecimal.valueOf(0.05))
                .allowNegative(false)
                .isEnabled(true)
                .build();
        
        assertEquals("test", currency.getId());
        assertEquals("Test Currency", currency.getName());
        assertEquals("$", currency.getSymbol());
        assertEquals("%s%s", currency.getFormat());
        assertEquals(2, currency.getDecimalPlaces());
        assertTrue(currency.isDefault());
        assertEquals(BigDecimal.valueOf(100), currency.getInitialBalance());
        assertEquals(BigDecimal.ZERO, currency.getMinBalance());
        assertEquals(BigDecimal.valueOf(1000000), currency.getMaxBalance());
        assertEquals(BigDecimal.valueOf(0.05), currency.getInterestRate());
        assertFalse(currency.isAllowNegative());
        assertTrue(currency.isEnabled());
    }
} 