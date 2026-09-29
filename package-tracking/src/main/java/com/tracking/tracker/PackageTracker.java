package com.tracking.tracker;
import com.tracking.carrier.CarrierService;
import com.tracking.exception.TrackingException;

import java.util.Objects;
public abstract class PackageTracker {

    protected final CarrierService carrier;

    protected PackageTracker(CarrierService carrier) {

        this.carrier = Objects.requireNonNull(carrier, "carrier");
    }

    public abstract String report(String trackingNumber) throws TrackingException;
}
