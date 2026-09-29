package com.tracking.tracker;

import com.tracking.carrier.CarrierService;
import com.tracking.exception.PackageNotFoundException;
import com.tracking.model.DeliveryStatus;
import com.tracking.model.TrackingInfo;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DetailedTrackerTest {

    private final CarrierService carrier = mock(CarrierService.class);

    @Test
    void delegatesToCarrierAndShowsAllFields() throws Exception {
        when(carrier.getName()).thenReturn("MockCarrier");
        when(carrier.fetch("MC-1")).thenReturn(
                new TrackingInfo("MC-1", DeliveryStatus.DELIVERED, "Astana", Instant.EPOCH));

        String report = new DetailedTracker(carrier).report("MC-1");

        assertTrue(report.contains("MC-1"));
        assertTrue(report.contains("MockCarrier"));
        assertTrue(report.contains("DELIVERED"));
        assertTrue(report.contains("Astana"));
        verify(carrier).fetch("MC-1");
    }

    @Test
    void propagatesCarrierFailure() throws Exception {
        when(carrier.fetch("MC-2")).thenThrow(new PackageNotFoundException("MC-2"));

        assertThrows(PackageNotFoundException.class, () -> new DetailedTracker(carrier).report("MC-2"));
    }
}
