package com.tracking.carrier;

import com.tracking.exception.PackageNotFoundException;
import com.tracking.exception.TrackingException;
import com.tracking.model.DeliveryStatus;
import com.tracking.model.TrackingInfo;

import java.time.Instant;
import java.util.Map;
public class GlobalExpressCarrier implements CarrierService {

    private final Map<String, TrackingInfo> parcels = Map.of(
            "GX-2002", new TrackingInfo("GX-2002", DeliveryStatus.OUT_FOR_DELIVERY,
                    "Shymkent", Instant.parse("2026-09-29T07:30:00Z")),
            "GX-2003", new TrackingInfo("GX-2003", DeliveryStatus.REGISTERED,
                    "Warehouse Dubai", Instant.parse("2026-09-29T02:00:00Z")));

    @Override
    public String getName() {

        return "GlobalExpress";
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
