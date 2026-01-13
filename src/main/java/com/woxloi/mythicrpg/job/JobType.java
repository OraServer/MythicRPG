package com.woxloi.mythicrpg.job;

public enum JobType {

    WARRIOR("戦士"),
    MAGE("魔法使い"),
    ARCHER("弓使い");

    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
