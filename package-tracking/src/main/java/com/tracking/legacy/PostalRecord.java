package com.tracking.legacy;
public class PostalRecord {
    private final int code;
    private final String statusText;
    private final String city;
    private final long updatedEpochSeconds;

    public PostalRecord(int code, String statusText, String city, long updatedEpochSeconds) {
        this.code = code;
        this.statusText = statusText;
        this.city = city;
        this.updatedEpochSeconds = updatedEpochSeconds;
    }

    public int getCode() {
        return code;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getCity() {
        return city;
    }

    public long getUpdatedEpochSeconds() {
        return updatedEpochSeconds;
    }
}
