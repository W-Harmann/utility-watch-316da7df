package com.utilitywatch;

// US9.1 - As a homeowner I wish to track my utility usage so that I can understand consumption habits into actual dollars spent.
//      [x] User can select either utility or both
//      [x] The user can set a time range to measure cost in a range.
//      [x] User can track there(their) estimated cost against there(their) set allocated budget for utilities.
//      [x] User can see the estimated cost of period based off inputted rates.
//      [x] Rates can be inputted and changed.
//      [x] Money is calculated and stored without floating-point rounding error, and figures always display to two decimal places.
//      [x] Negative numbers aren’t accepted.
//

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class CostBudgetEvaluatorTest {

    private CostBudgetEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new CostBudgetEvaluator();
    }

        //
        // Criteria: The user can set a time range to measure cost in a range
        //

    // Set-time range is stored into 2 variables LocalDateTime
        // Local DateTime fromDate
        // Local DateTime toDate
    // **Variable names can be arbitrary
    @Test
    void shouldBeStoringTimeRange() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 8, 2, 0, 0);

        // store to class
        evaluator.setTimeRange(fromDate, toDate);

        // init expected data (fromDate, toDate)
        List<LocalDateTime> ExpectedTimeRange =
                new ArrayList<>(
                        List.of(fromDate,
                                toDate)
                );

        // get stored time range
        List<LocalDateTime> ActualTimeRange = evaluator.getTimeRange();

        // expects time range returned in Array Format
        // { fromDate, toDate }
        assertEquals(
                ExpectedTimeRange,
                ActualTimeRange
        );
    }

    // Set-time range is invalid | toDate is less-recent than fromDate (not expected)
    // In this case, the app should probably tell the user to fix the date inputs
    @Test
    void shouldBeReturningInvalidTimeRange() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 7, 1, 0, 0);

        // store to class
        evaluator.setTimeRange(fromDate, toDate);

        // get stored time range - validity
        boolean ValidTimeRange = evaluator.getTimeRangeValidity();

        // expects return of False
        assertFalse(ValidTimeRange);
    }

        //
        // Criteria: Negative numbers aren't expected
        //

    // INPUTTED rates for utilities should be POSITIVE
    // ** Assuming the budget class stores a hashmap/dictionary for each utility cost rate INPUTTED
    // ** NEGATIVE inputs will be return as default: 0

    // Test 1
    @Test
    void shouldStoreInputBudget() {

        // store to class
        evaluator.setMonthlyBudget(124);

        assertEquals(
                124,
                evaluator.getMonthlyBudget()
        );
    }

    // Test 2
    @Test
    void shouldNotStoreInputBudget() {

        // store to class
        evaluator.setMonthlyBudget(500);
        evaluator.setMonthlyBudget(-124);

        assertEquals(
                500,
                evaluator.getMonthlyBudget()
        );
    }

    // Test 3
    @Test
    void shouldStoreInputPositivePower() {

        // assuming 1 kWh = 2.5 dollars
        evaluator.setUtilityCost("energy", 2.5);

        assertEquals(
                2.5,
                evaluator.getUtilityCost("energy")
        );
    }

    // Test 4
    @Test
    void shouldStoreInputPositiveWater() {

        // assuming 1 kLh = 1.5 dollars
        evaluator.setUtilityCost("water", 1.5);

        assertEquals(
                1.5,
                evaluator.getUtilityCost("water")
        );
    }

    // Negative inputs don't store and return as their default values (previous value).
    // Test 5
    @Test
    void shouldNotStoreInputNegativePower() {

        evaluator.setUtilityCost("energy", 1);
        evaluator.setUtilityCost("energy", -2.5);

        assertEquals(
                1,
                evaluator.getUtilityCost("energy")
        );
    }

    // Test 6
    @Test
    void shouldNotStoreInputNegativeWater() {

        evaluator.setUtilityCost("water", 4);
        evaluator.setUtilityCost("water", -2.5);

        assertEquals(
                4,
                evaluator.getUtilityCost("water")
        );
    }

    // Because to calculate costs, the app needs to know the readings

    @Test
    void shouldStoreReadings() {

        // init expected data
        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);

        assertEquals(
                readings,
                evaluator.getReadings()
        );
    }

    //
    // Criteria: Money is calculated and stored without floating-point rounding error, and figures always display to two decimal places.
    //

    // ** Assuming all tests above passed

    // Test 1
    @Test
    void shouldCalculateTimeRangeUtilityCostPower() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        // assuming 1 kWh = 2.5 dollars
        evaluator.setUtilityCost("energy", 2.5);

        // Inclusive Power Reading (3 + 5 + 3) * Cost (2.5) = 27.5
        assertEquals(
                27.5,
                evaluator.calculateUtilityCost("energy")
        );
    }

    // Test 2
    @Test
    void shouldCalculateTimeRangeUtilityCostWater() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        // assuming 1 kLh = 1.5 dollars
        evaluator.setUtilityCost("water", 1.5);

        // Inclusive Water Reading (4) * Cost (1.5) = 6
        assertEquals(
                6.0,
                evaluator.calculateUtilityCost("water")
        );
    }

    // Test 3
    @Test
    void shouldCalculateTimeRangeUtilityCostBoth() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        // assuming costs
        evaluator.setUtilityCost("energy", 2.5);
        evaluator.setUtilityCost("water", 1.5);

        // Inclusive Power Reading (3 + 5 + 3) * Cost (2.5) = 27.5
        // Inclusive Water Reading (4) * Cost (1.5) = 6
        // Sum (27.5 + 6) = 33.5
        assertEquals(
                33.5,
                evaluator.calculateUtilityCost("both")
        );
    }

    // Test 4
    @Test
    void shouldCalculateTimeRangeUtilityCostDecimal() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 1.01, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 2.7, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 9.17, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        // assuming costs
        evaluator.setUtilityCost("energy", 9.36);

        // Inclusive Power Reading (1.01 + 2.7 + 9.17) * Cost (9.36) = 120.5568
        // Rounded to 2 decimal places: 120.5568 -> 120.56
        assertEquals(
                120.56,
                evaluator.calculateUtilityCost("energy")
        );
    }


    //
    // Criteria: User can track their estimated cost against their set allocated budget for utilities.
    //

    // Sample data: energy (3 + 5 + 3) * 2.5 = 27.50, water (4) * 1.5 = 6.00 -> 33.50 spent in August
    private void setUpAugustSpending() {
        List<Reading> readings = new ArrayList<>();
        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        evaluator.setReadings(readings);
        evaluator.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 9, 1, 0, 0));
        evaluator.setUtilityCost("energy", 2.5);
        evaluator.setUtilityCost("water", 1.5);
    }

    // Percentage of budget used

    @Test
    void shouldCalculateBudgetPercentageWhenUnderBudget() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(50);

        // 33.50 / 50.00 = 67%
        assertEquals(67.0, evaluator.getBudgetUsedPercentage());
    }

    @Test
    void shouldCalculateBudgetPercentageWhenExactlyOnBudget() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(33.5);

        assertEquals(100.0, evaluator.getBudgetUsedPercentage());
    }

    @Test
    void shouldCalculateBudgetPercentageAboveHundredWhenOverBudget() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(30);

        // 33.50 / 30.00 = 111.666...% -> 111.67% (the UI caps the bar, the evaluator reports the true figure)
        assertEquals(111.67, evaluator.getBudgetUsedPercentage());
    }

    // Amount remaining

    @Test
    void shouldCalculateBudgetRemainingWhenUnderBudget() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(50);

        // 50.00 - 33.50 = 16.50
        assertEquals(16.5, evaluator.getBudgetRemaining());
    }

    @Test
    void shouldReturnZeroRemainingWhenOverBudget() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(30);

        // never negative - isOverBudget() covers this case
        assertEquals(0.0, evaluator.getBudgetRemaining());
    }

    // Over budget

    @Test
    void shouldNotBeOverBudgetWhenUnderBudget() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(50);

        assertFalse(evaluator.isOverBudget());
    }

    @Test
    void shouldNotBeOverBudgetWhenExactlyOnBudget() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(33.5);

        // reaching the limit is not going over it
        assertFalse(evaluator.isOverBudget());
    }

    @Test
    void shouldBeOverBudgetWhenSpendingExceedsBudget() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(30);

        assertTrue(evaluator.isOverBudget());
    }

    // No budget set

    @Test
    void shouldHandleNoBudgetSetWithoutDividingByZero() {
        setUpAugustSpending();
        evaluator.setMonthlyBudget(0);

        assertEquals(0.0, evaluator.getBudgetUsedPercentage());
        assertEquals(0.0, evaluator.getBudgetRemaining());
        assertFalse(evaluator.isOverBudget());
    }


}