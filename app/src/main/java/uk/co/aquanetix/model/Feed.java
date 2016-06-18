package uk.co.aquanetix.model;

public class Feed {

    private final int feedId;
    private final String feedname;
    private final Float min_weight;
    private final Float max_weight;
    private final Float weightPerUnit;

    public Feed(int feedId, String feedname, Float min_weight, Float max_weight, Float weightPerUnit) {
        this.feedId = feedId;
        this.feedname = feedname;
        this.min_weight = min_weight;
        this.max_weight = max_weight;
        this.weightPerUnit = weightPerUnit;
    }
    
    public int getFeedId() { return feedId; }
    public String getFeedname() { return feedname; }
    public Float getMinWeight() { return min_weight; }
    public Float getMaxWeight() { return max_weight; }
    public Float getWeightPerUnit() { return weightPerUnit; }

}
