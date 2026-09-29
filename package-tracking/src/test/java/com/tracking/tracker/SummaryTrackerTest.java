package com.tracking.tracker;

import com.tracking.carrier.CarrierService;
import com.tracking.exception.CarrierUnavailableException;
import com.tracking.model.DeliveryStatus;
import com.tracking.model.TrackingInfo;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SummaryTrackerTest {

    private final CarrierService carrier = mock(CarrierService.class);

    @Test
    void delegatesToCarrierAndFormatsOneLine() throws Exception {
        when(carrier.getName()).thenReturn("MockCarrier");
        when(carrier.fetch("MC-1")).thenReturn(
                new TrackingInfo("MC-1", DeliveryStatus.IN_TRANSIT, "Hub", Instant.EPOCH));

        String report = new SummaryTracker(carrier).report("MC-1");

        assertEquals("[MockCarrier] MC-1 -> IN_TRANSIT", report);
        verify(carrier).fetch("MC-1");
    }

    @Test
    void propagatesCarrierFailure() throws Exception {
        when(carrier.fetch("MC-2")).thenThrow(new CarrierUnavailableException("MockCarrier", "down"));

        assertThrows(CarrierUnavailableException.class, () -> new SummaryTracker(carrier).report("MC-2"));
    }
}
