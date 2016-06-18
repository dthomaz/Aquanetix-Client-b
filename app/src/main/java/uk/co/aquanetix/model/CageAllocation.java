package uk.co.aquanetix.model;

public class CageAllocation {
    
    //Cage allocation-specific
    private int cageAllocationId;
    private long tm; //timestamp, created at
    private int userId;
    //Jobs left to do
    private boolean feedDone;
    private boolean mortalitiesDone;
    private boolean netDone;
    private boolean healthDone;
    private boolean temperatureDone; //Only for tanks/ponds
    private boolean oxygenDone; //Only for tanks/ponds
    //Cage-specific
    private int cageId;
    private String cageName;
    private int individuals;
    private String cageType;
    //Feed (only one supported)
    private int feedingId;
    private String feedType;
    private int feedId;
    private float feedQuantityApproved;
    private float feedQuantityUsed;
    private float meanWeightGr;
    private String units;

    
    public int getCageAllocationId() { return cageAllocationId; }
    public long getTm() { return tm; }
    public int getUserId() { return userId; }
    public boolean isFeedDone() { return feedDone; }
    public boolean isMortalitiesDone() { return mortalitiesDone; }
    public boolean isNetDone() { return netDone; }
    public boolean isHealthDone() { return healthDone; }
    public boolean isTemperatureDone() { return temperatureDone; }
    public boolean isOxygenDone() { return oxygenDone; }
    public int getCageId() { return cageId; }
    public String getCageName() { return cageName; }
    public int getIndividuals() { return individuals; }
    public String getCageType() { return cageType; }
    public int getFeedingId() { return feedingId; }
    public String getFeedType() { return feedType; }
    public int getFeedId() {return feedId;}
    public float getFeedQuantityApproved() { return feedQuantityApproved; }
    public float getFeedQuantityUsed() { return feedQuantityUsed; }
    public float getMeanWeightGr() { return meanWeightGr; }
    public String getUnits() { return units; }

    
    public void setCageAllocationId(int cageAllocationId) { this.cageAllocationId = cageAllocationId; }
    public void setTm(long tm) { this.tm = tm; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setFeedDone(boolean feedDone) { this.feedDone = feedDone; }
    public void setMortalitiesDone(boolean mortalitiesDone) { this.mortalitiesDone = mortalitiesDone; }
    public void setNetDone(boolean netDone) { this.netDone = netDone; }
    public void setHealthDone(boolean healthDone) { this.healthDone = healthDone; }
    public void setTemperatureDone(boolean temperatureDone) { this.temperatureDone = temperatureDone; }
    public void setOxygenDone(boolean oxygenDone) { this.oxygenDone = oxygenDone; }
    public void setCageId(int cageId) { this.cageId = cageId; }
    public void setCageName(String cageName) { this.cageName = cageName; }
    public void setIndividuals(int individuals) { this.individuals = individuals; }
    public void setCageType(String cageType) { this.cageType = cageType; }
    public void setFeedingId(int feedingId) { this.feedingId = feedingId; }
    public void setFeedType(String feedType) { this.feedType = feedType; }
    public void setFeedId(int feedId) { this.feedId = feedId; }
    public void setFeedQuantityApproved(float feedQuantityApproved) { this.feedQuantityApproved = feedQuantityApproved; }
    public void setFeedQuantityUsed(float feedQuantityUsed) { this.feedQuantityUsed = feedQuantityUsed; }
    public void setMeanWeightGr(float meanWeightGr) { this.meanWeightGr = meanWeightGr; }
    public void setUnits(String units) { this.units = units; }
    
}
