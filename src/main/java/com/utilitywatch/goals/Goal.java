package com.utilitywatch.goals;

/**
 * A reduction goal for energy or water, including how much the user wants
 * to cut their usage, over what period, and how far they have got.
 * Goal values are fixed once created except for the database id.
 */
public class Goal {

    private final String resourceType;
    private final double targetReduction;
    private final String targetPeriod;
    private final boolean completed;
    private final double currentProgress;

    private int id; // assigned by the database on insert, 0 until then

    /** @return the database id, or 0 if the goal has not been saved yet */
    public int getId() {
        return id;
    }

    /**
     * sets the database id, called by the DAO and GoalRepository
     * @param id the id assigned by the database
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * creates a goal with no progress yet and calls the full constructor with progress set to 0
     * @param resourceType energy or water
     * @param targetReduction the percentage reduction to achieve
     * @param targetPeriod the period label such as '1 Month'
     * @param completed whether the goal has already been achieved
     */
    // keep original constructor for existing tests
    public Goal(String resourceType, double targetReduction, String targetPeriod, boolean completed) {
        this(resourceType, targetReduction, targetPeriod, completed, 0.0);
    }

    /**
     * creates a goal that includes the progress made so far (us8.4, us8.4)
     * @param resourceType energy or water
     * @param targetReduction the percentage reduction to achieve
     * @param targetPeriod the period label such as '1 Month'
     * @param completed whether the goal has been achieved
     * @param currentProgress the percentage reduction achieved so far
     */
    public Goal(String resourceType, double targetReduction, String targetPeriod, boolean completed, double currentProgress) {
        this.resourceType = resourceType;
        this.targetReduction = targetReduction;
        this.targetPeriod = targetPeriod;
        this.completed = completed;
        this.currentProgress = currentProgress;
    }

    /**
     * @return the utility this goal applies to (energy or water)
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * @return the percentage reduction the user wants to achieve
     */
    public double getTargetReduction() {
        return targetReduction;
    }

    /**
     * @return the period the goal covers, such as 3 months
     */
    public String getTargetPeriod() {
        return targetPeriod;
    }

    /**
     * @return true if the target reduction has been reached
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * @return the percentage reduction achieved so far
     */
    public double getCurrentProgress() {
        return currentProgress;
    }
}