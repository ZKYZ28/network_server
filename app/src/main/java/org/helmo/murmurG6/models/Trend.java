package org.helmo.murmurG6.models;

public class Trend {

    private final String trendName;
    private final String domain;

    public Trend(String trendName, String domain) {
        this.trendName = trendName;
        this.domain = domain;
    }

    public String getTrendName() {
        return trendName;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return trendName + "@" + domain;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Trend trend = (Trend) obj;
        return trend.trendName.equals(this.trendName);
    }
}