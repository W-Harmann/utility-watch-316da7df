package com.utilitywatch.controller;

// Settings shared by the Costs and Impact pages, so values survive switching between pages.
// Held in memory only for now: they reset when the app restarts (saving them is a later story).

/**
 * Settings shared by the Costs and Impact pages, so values survive switching between pages.
 */
public final class SharedUtilitySettings {

    // MOCK DATA: example prices taken from the design, used until the user enters their own
    /** Default energy price in dollars per kWh (28.5c), used until the user enters their own rate. */
    public static final double MOCK_ENERGY_RATE = 0.285;     // $ per kWh (28.5c)
    /** Default water price in dollars per litre ($3.10 per kL), used until the user enters their own rate. */
    public static final double MOCK_WATER_RATE = 0.0031;     // $ per litre ($3.10 per kL)

    // MOCK DATA: placeholder emission factors until real figures are sourced
    /** Placeholder energy emission factor in kg CO2e per kWh, used until real figures are sourced. */
    public static final double MOCK_ENERGY_EMISSION = 0.7;   // kg CO2e per kWh
    /** Placeholder water emission factor in kg CO2e per litre, used until real figures are sourced. */
    public static final double MOCK_WATER_EMISSION = 0.0005; // kg CO2e per litre

    private static double energyRate = MOCK_ENERGY_RATE;
    private static double waterRate = MOCK_WATER_RATE;
    private static double energyEmission = MOCK_ENERGY_EMISSION;
    private static double waterEmission = MOCK_WATER_EMISSION;
    private static double monthlyBudget = 0; // 0 = no budget set

    private SharedUtilitySettings() {
    }

    // Callers validate values (non-negative numbers) before setting them.

    /**
     * Returns the current energy price.
     *
     * @return the energy rate in dollars per kWh
     */
    public static double getEnergyRate() { return energyRate; }

    /**
     * Sets the energy price. The caller is expected to have validated the value.
     *
     * @param rate the new energy rate in dollars per kWh (non-negative)
     */
    public static void setEnergyRate(double rate) { energyRate = rate; }

    /**
     * Returns the current water price.
     *
     * @return the water rate in dollars per litre
     */
    public static double getWaterRate() { return waterRate; }

    /**
     * Sets the water price. The caller is expected to have validated the value.
     *
     * @param rate the new water rate in dollars per litre (non-negative)
     */
    public static void setWaterRate(double rate) { waterRate = rate; }

    /**
     * Returns the current energy emission factor.
     *
     * @return the energy emission factor in kg CO2e per kWh
     */
    public static double getEnergyEmission() { return energyEmission; }

    /**
     * Sets the energy emission factor. The caller is expected to have validated the value.
     *
     * @param kgPerKwh the new emission factor in kg CO2e per kWh (non-negative)
     */
    public static void setEnergyEmission(double kgPerKwh) { energyEmission = kgPerKwh; }

    /**
     * Returns the current water emission factor.
     *
     * @return the water emission factor in kg CO2e per litre
     */
    public static double getWaterEmission() { return waterEmission; }

    /**
     * Sets the water emission factor. The caller is expected to have validated the value.
     *
     * @param kgPerLitre the new emission factor in kg CO2e per litre (non-negative)
     */
    public static void setWaterEmission(double kgPerLitre) { waterEmission = kgPerLitre; }

    /**
     * Puts every setting back to its starting value (the mock rates and emission factors, and no
     * budget), for example when an account is deleted.
     */
    public static void reset() {
        energyRate = MOCK_ENERGY_RATE;
        waterRate = MOCK_WATER_RATE;
        energyEmission = MOCK_ENERGY_EMISSION;
        waterEmission = MOCK_WATER_EMISSION;
        monthlyBudget = 0;
    }

    /**
     * Returns the user's monthly budget.
     *
     * @return the monthly budget in dollars, or {@code 0} if no budget has been set
     */
    public static double getMonthlyBudget() { return monthlyBudget; }

    /**
     * Sets the user's monthly budget. The caller is expected to have validated the value.
     *
     * @param budget the monthly budget in dollars, or {@code 0} for no budget
     */
    public static void setMonthlyBudget(double budget) { monthlyBudget = budget; }
}
