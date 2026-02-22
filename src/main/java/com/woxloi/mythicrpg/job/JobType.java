package com.woxloi.mythicrpg.job;
public enum JobType {
    WARRIOR("戦士"), MAGE("魔法使い"), ARCHER("弓使い");
    private final String displayName;
    JobType(String d) { this.displayName = d; }
    public String getDisplayName() { return displayName; }
}
