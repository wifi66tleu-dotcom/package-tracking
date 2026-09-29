package com.tracking.legacy;
import java.util.Map;
public class LegacyPostalSystem {

    public static final int OK = 0;
    public static final int NOT_FOUND = 404;
    public static final int SERVICE_DOWN = 503;

    private final Map<Integer, PostalRecord> records = Map.of(
            3003, new PostalRecord(OK, "ON_THE_WAY", "Karaganda", 1790000000L),
            3004, new PostalRecord(OK, "HANDED_OVER", "Astana", 1790050000L));

    private boolean online = true;

    public PostalRecord queryParcel(int parcelId, String language) {
        if (!online) {
            return new PostalRecord(SERVICE_DOWN, null, null, 0);
        }
        PostalRecord record = records.get(parcelId);
        return record != null ? record : new PostalRecord(NOT_FOUND, null, null, 0);
    }

    public void setOnline(boolean online) {

        this.online = online;
    }
}
