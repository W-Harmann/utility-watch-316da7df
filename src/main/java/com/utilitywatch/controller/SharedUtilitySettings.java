package com.utilitywatch.controller;

// Settings shared by the Costs and Impact pages, so values survive switching between pages.
// Held in memory only for now: they reset when the app restarts (saving them is a later story).
public final class SharedUtilitySettings {

    // MOCK DATA: example prices taken from the design, used until the user enters their own
    public static final double MOCK_ENERGY_RATE = 0.285;     // $ per kWh (28.5c)
    public static final double MOCK_WATER_RATE = 0.0031;     // $ per litre ($3.10 per kL)

    // MOCK DATA: placeholder emission factors until real figures are sourced
    public static final double MOCK_ENERGY_EMISSION = 0.7;   // kg CO2e per kWh
    public static final double MOCK_WATER_EMISSION = 0.0005; // kg CO2e per litre

    private static double energyRate = MOCK_ENERGY_RATE;
    private static double waterRate = MOCK_WATER_RATE;
    private static double energyEmission = MOCK_ENERGY_EMISSION;
    private static double waterEmission = MOCK_WATER_EMISSION;
    private static double monthlyBudget = 0; // 0 = no budget set

    private SharedUtilitySettings() {
    }

    // Callers validate values (non-negative numbers) before setting them.

    public static double getEnergyRate() { return energyRate; }
    public static void setEnergyRate(double rate) { energyRate = rate; }

    public static double getWaterRate() { return waterRate; }
    public static void setWaterRate(double rate) { waterRate = rate; }

    public static double getEnergyEmission() { return energyEmission; }
    public static void setEnergyEmission(double kgPerKwh) { energyEmission = kgPerKwh; }

    public static double getWaterEmission() { return waterEmission; }
    public static void setWaterEmission(double kgPerLitre) { waterEmission = kgPerLitre; }

    public static double getMonthlyBudget() { return monthlyBudget; }
    public static void setMonthlyBudget(double budget) { monthlyBudget = budget; }
}
