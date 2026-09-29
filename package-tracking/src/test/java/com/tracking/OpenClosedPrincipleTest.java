package com.tracking;

import com.tracking.carrier.CarrierService;
import com.tracking.exception.TrackingException;
import com.tracking.model.DeliveryStatus;
import com.tracking.model.TrackingInfo;
import com.tracking.registry.CarrierRegistry;
import com.tracking.tracker.DetailedTracker;
import com.tracking.tracker.PackageTracker;
import com.tracking.tracker.SummaryTracker;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenClosedPrincipleTest {

    static class DroneCarrier implements CarrierService {
        @Override
        public String getName() {

            return "Drone";
        }

        @Override
        public TrackingInfo fetch(String trackingNumber) throws TrackingException {
            return new TrackingInfo(trackingNumber, DeliveryStatus.OUT_FOR_DELIVERY, "Sky", Instant.EPOCH);
        }
    }

    static class ShoutingTracker extends PackageTracker {
        ShoutingTracker(CarrierService carrier) {

            super(carrier);
        }

        @Override
        public String report(String trackingNumber) throws TrackingException {
            return carrier.fetch(trackingNumber).status().name().toUpperCase() + "!!!";
        }
    }

    @Test
    void newImplementorWorksWithExistingAbstractions() throws Exception {
        CarrierRegistry registry = new CarrierRegistry();
        registry.register("DR", new DroneCarrier());
        CarrierService carrier = registry.resolve("DR-1");

        assertEquals("[Drone] DR-1 -> OUT_FOR_DELIVERY", new SummaryTracker(carrier).report("DR-1"));
        assertTrue(new DetailedTracker(carrier).report("DR-1").contains("Sky"));
    }

    @Test
    void newAbstractionWorksWithExistingImplementor() throws Exception {
        PackageTracker tracker = new ShoutingTracker(new DroneCarrier());

        assertEquals("OUT_FOR_DELIVERY!!!", tracker.report("DR-1"));
    }
}
