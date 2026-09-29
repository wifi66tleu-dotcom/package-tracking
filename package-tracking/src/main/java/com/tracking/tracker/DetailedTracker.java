package com.tracking.tracker;
import com.tracking.carrier.CarrierService;
import com.tracking.exception.TrackingException;
import com.tracking.model.TrackingInfo;

public class DetailedTracker extends PackageTracker {

    public DetailedTracker(CarrierService carrier) {

        super(carrier);
    }

    @Override
    public String report(String trackingNumber) throws TrackingException {
        TrackingInfo info = carrier.fetch(trackingNumber);
        return "Package:  " + info.trackingNumber() + "\n"
             + "Carrier:  " + carrier.getName() + "\n"
             + "Status:   " + info.status() + "\n"
             + "Location: " + info.location() + "\n"
             + "Updated:  " + info.updatedAt();
    }
}
