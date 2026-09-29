package com.tracking;

import com.tracking.adapter.LegacyPostalAdapter;
import com.tracking.carrier.CarrierService;
import com.tracking.carrier.FastPostCarrier;
import com.tracking.carrier.GlobalExpressCarrier;
import com.tracking.exception.TrackingException;
import com.tracking.legacy.LegacyPostalSystem;
import com.tracking.registry.CarrierRegistry;
import com.tracking.tracker.DetailedTracker;
import com.tracking.tracker.PackageTracker;
import com.tracking.tracker.SummaryTracker;

import java.util.function.Function;
public class Main {

    public static void main(String[] args) {
        LegacyPostalSystem legacy = new LegacyPostalSystem();

        CarrierRegistry registry = new CarrierRegistry();
        registry.register("FP", new FastPostCarrier());
        registry.register("GX", new GlobalExpressCarrier());
        registry.register("LP", new LegacyPostalAdapter(legacy));

        System.out.println("--- Summary tracker ---");
        for (String number : new String[]{"FP-1001", "GX-2002", "LP-3003"}) {
            track(registry, number, SummaryTracker::new);
        }

        System.out.println("\n--- Detailed tracker ---");
        for (String number : new String[]{"FP-1002", "GX-2003", "LP-3004"}) {
            track(registry, number, DetailedTracker::new);
        }

        System.out.println("\n--- Failures ---");
        track(registry, "FP-0000", SummaryTracker::new);   // unknown package (native)
        track(registry, "LP-9999", SummaryTracker::new);   // unknown package (adapted, code 404)
        track(registry, "LP-abc", SummaryTracker::new);    // malformed number (adapted)
        track(registry, "ZZ-1", SummaryTracker::new);      // no such carrier
        legacy.setOnline(false);
        track(registry, "LP-3003", SummaryTracker::new);   // legacy system down (code 503)
    }
    private static void track(CarrierRegistry registry, String trackingNumber,
                              Function<CarrierService, PackageTracker> trackerType) {
        try {
            CarrierService carrier = registry.resolve(trackingNumber);
            PackageTracker tracker = trackerType.apply(carrier);
            System.out.println(tracker.report(trackingNumber));
        } catch (TrackingException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
