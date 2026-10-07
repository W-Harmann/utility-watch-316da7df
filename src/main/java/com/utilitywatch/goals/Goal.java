package com.utilitywatch.goals;

public class Goal {

    private final String resourceType;
    private final double targetReduction;
    private final String targetPeriod;
    private final boolean completed;
    private final double currentProgress;

    // keep original constructor for existing tests
    public Goal(String resourceType, double targetReduction, String targetPeriod, boolean completed) {
        this(resourceType, targetReduction, targetPeriod, completed, 0.0);
    }

    // full constructor that includes progress tracking for us8.4 and us8.5
    public Goal(String resourceType, double targetReduction, String targetPeriod, boolean completed, double currentProgress) {
        this.resourceType = resourceType;
        this.targetReduction = targetReduction;
        this.targetPeriod = targetPeriod;
        this.completed = completed;
        this.currentProgress = currentProgress;
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

    public double getCurrentProgress() {
        return currentProgress;
    }
}