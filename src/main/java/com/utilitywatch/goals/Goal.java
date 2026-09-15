package com.utilitywatch.goals;

public class Goal {

    private final String resourceType;
    private final double targetReduction;
    private final String targetPeriod;
    private final boolean completed;

    public Goal(String resourceType, double targetReduction, String targetPeriod, boolean completed) {
        this.resourceType = resourceType;
        this.targetReduction = targetReduction;
        this.targetPeriod = targetPeriod;
        this.completed = completed;
    }

    public String getResourceType() {
        return resourceType;
    }

    public double getTargetReduction() {
        return targetReduction;
    }

    public String getTargetPeriod() {
        return targetPeriod;
    }

    public boolean isCompleted() {
        return completed;
    }
}