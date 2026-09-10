package com.utilitywatch;

import org.junit.jupiter.api.*;

import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.*;

public class UtilityValidityTest {
    private Reading reading;

    @BeforeEach
    void setUp() {
        reading = new Reading();
    }

    @Test
    void ShouldThrowExceptionAtNull() {
        assertThrows(IllegalArgumentException.class, () -> reading.setQuantity(null));
    }

    @Test
    void ShouldThrowExceptionAtNegative() {
        assertThrows(IllegalArgumentException.class, () -> reading.setQuantity(-1));
    }

    @Test
    void ShouldThrowExceptionAtNaN() {
        assertThrows(IllegalArgumentException.class, () -> reading.setQuantity(NaN));
    }

    @Test
    void ShouldThrowExceptionAtString() {
        assertThrows(IllegalArgumentException.class, () -> reading.setQuantity("Banana"));
    }

    @Test
    void ShouldStoreInteger() {
        reading.setQuantity(3);
        assertEquals(3, reading.getQuantity());
    }

    @Test
    void ShouldStoreDecimal() {
        reading.setQuantity(6.7);
        assertEquals(6.7, reading.getQuantity());
    }



}
