package com.example.demo.dto;

public class MissionStatusDto {
    private final boolean isMissionCompleted;
    private final boolean isRewardClaimed;
    private final int rewardXp;
    private final String missionText;

    public MissionStatusDto(boolean isMissionCompleted, boolean isRewardClaimed, int rewardXp, String missionText) {
        this.isMissionCompleted = isMissionCompleted;
        this.isRewardClaimed = isRewardClaimed;
        this.rewardXp = rewardXp;
        this.missionText = missionText;
    }

    public boolean isMissionCompleted() { return isMissionCompleted; }
    public boolean isRewardClaimed() { return isRewardClaimed; }
    public int getRewardXp() { return rewardXp; }
    public String getMissionText() { return missionText; }
}