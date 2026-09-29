package com.tracking.tracker;
import com.tracking.carrier.CarrierService;
import com.tracking.exception.TrackingException;
import com.tracking.model.TrackingInfo;

public class SummaryTracker extends PackageTracker {

    public SummaryTracker(CarrierService carrier) {

        super(carrier);
    }

    @Override
    public String report(String trackingNumber) throws TrackingException {
        TrackingInfo info = carrier.fetch(trackingNumber);
        return "[" + carrier.getName() + "] " + info.trackingNumber() + " -> " + info.status();
    }
}
