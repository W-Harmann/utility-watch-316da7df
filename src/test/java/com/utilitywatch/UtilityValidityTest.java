package com.utilitywatch;

import org.junit.jupiter.api.*;

import static java.lang.Float.NaN;
import static org.junit.jupiter.api.Assertions.*;

public class UtilityValidityTest {
    private Utility utility;

    @BeforeEach
    void setUp() {
        utility = new Utility();
    }

    @Test
    void ShouldThrowExceptionAtNull() {
        assertThrows(IllegalArgumentException.class, () -> utility.setQuantity(null));
    }

    @Test
    void ShouldThrowExceptionAtNegative() {
        assertThrows(IllegalArgumentException.class, () -> utility.setQuantity(-1));
    }

    @Test
    void ShouldThrowExceptionAtNaN() {
        assertThrows(IllegalArgumentException.class, () -> utility.setQuantity(NaN));
    }

    @Test
    void ShouldThrowExceptionAtString() {
        assertThrows(IllegalArgumentException.class, () -> utility.setQuantity("Banana"));
    }

    @Test
    void ShouldStoreInteger() {
        utility.setQuantity(3);
        assertEquals(3, utility.getQuantity());
    }

    @Test
    void ShouldStoreDecimal() {
        utility.setQuantity(6.7);
        assertEquals(6.7, utility.getQuantity());
    }



}
