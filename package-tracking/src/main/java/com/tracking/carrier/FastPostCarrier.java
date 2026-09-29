package com.tracking.carrier;

import com.tracking.exception.PackageNotFoundException;
import com.tracking.exception.TrackingException;
import com.tracking.model.DeliveryStatus;
import com.tracking.model.TrackingInfo;

import java.time.Instant;
import java.util.Map;
public class FastPostCarrier implements CarrierService {

    private final Map<String, TrackingInfo> parcels = Map.of(
            "FP-1001", new TrackingInfo("FP-1001", DeliveryStatus.IN_TRANSIT,
                    "Almaty sorting hub", Instant.parse("2026-09-28T10:15:00Z")),
            "FP-1002", new TrackingInfo("FP-1002", DeliveryStatus.DELIVERED,
                    "Astana", Instant.parse("2026-09-27T16:40:00Z")));

    @Override
    public String getName() {

        return "FastPost";
    }

    @Override
    public TrackingInfo fetch(String trackingNumber) throws TrackingException {
        TrackingInfo info = parcels.get(trackingNumber);
        if (info == null) {
            throw new PackageNotFoundException(trackingNumber);
        }
        return info;
    }
}
