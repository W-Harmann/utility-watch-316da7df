package com.utilitywatch;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UtilityValidityTest {
    private Utility utility;

    @BeforeEach
    void setUp() {
        utility = new Utility();
    }

    @Test
    void testUtilityNotNull() {
        assertNotNull(utility.getQuantity());
    }

    @Test
    void testUtilityNotNegative() {
        assertFalse(utility.getQuantity() < 0);
    }

    @Test
    void testUtilityWithInteger() {
        utility.setQuantity(3);
        assertEquals(3, utility.getQuantity());
    }

    @Test
    void testUtilityWithDecimal() {
        utility.setQuantity(6.7);
        assertEquals(6.7, utility.getQuantity());
    }



}
