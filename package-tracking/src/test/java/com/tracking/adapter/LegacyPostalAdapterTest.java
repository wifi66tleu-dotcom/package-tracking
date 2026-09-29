package com.tracking.adapter;

import com.tracking.exception.CarrierUnavailableException;
import com.tracking.exception.PackageNotFoundException;
import com.tracking.legacy.LegacyPostalSystem;
import com.tracking.legacy.PostalRecord;
import com.tracking.model.DeliveryStatus;
import com.tracking.model.TrackingInfo;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LegacyPostalAdapterTest {

    private final LegacyPostalSystem legacy = mock(LegacyPostalSystem.class);
    private final LegacyPostalAdapter adapter = new LegacyPostalAdapter(legacy);

    @Test
    void translatesNumberAndRecordOnSuccess() throws Exception {
        when(legacy.queryParcel(3003, "en"))
                .thenReturn(new PostalRecord(LegacyPostalSystem.OK, "WITH_COURIER", "Astana", 1000L));

        TrackingInfo info = adapter.fetch("LP-3003");

        assertEquals("LP-3003", info.trackingNumber());
        assertEquals(DeliveryStatus.OUT_FOR_DELIVERY, info.status());
        assertEquals("Astana", info.location());
        assertEquals(Instant.ofEpochSecond(1000L), info.updatedAt());
        verify(legacy).queryParcel(3003, "en");
    }


    @Test
    void notFoundCodeBecomesPackageNotFoundException() {
        when(legacy.queryParcel(1, "en"))
                .thenReturn(new PostalRecord(LegacyPostalSystem.NOT_FOUND, null, null, 0));

        PackageNotFoundException e =
                assertThrows(PackageNotFoundException.class, () -> adapter.fetch("LP-1"));
        assertEquals("Package not found: LP-1", e.getMessage());
    }

    @Test
    void serviceDownCodeBecomesCarrierUnavailableWithoutLeakingTheCode() {
        when(legacy.queryParcel(1, "en"))
                .thenReturn(new PostalRecord(LegacyPostalSystem.SERVICE_DOWN, null, null, 0));

        CarrierUnavailableException e =
                assertThrows(CarrierUnavailableException.class, () -> adapter.fetch("LP-1"));
        assertFalse(e.getMessage().contains("503"));
    }

    @Test
    void unknownErrorCodeBecomesCarrierUnavailable() {
        when(legacy.queryParcel(1, "en")).thenReturn(new PostalRecord(401, null, null, 0));

        assertThrows(CarrierUnavailableException.class, () -> adapter.fetch("LP-1"));
    }

    @Test
    void nullResponseBecomesCarrierUnavailable() {
        when(legacy.queryParcel(1, "en")).thenReturn(null);

        assertThrows(CarrierUnavailableException.class, () -> adapter.fetch("LP-1"));
    }

    @Test
    void unrecognisedStatusTextBecomesCarrierUnavailable() {
        when(legacy.queryParcel(1, "en"))
                .thenReturn(new PostalRecord(LegacyPostalSystem.OK, "LOST_IN_SPACE", "X", 0));

        assertThrows(CarrierUnavailableException.class, () -> adapter.fetch("LP-1"));
    }

    @Test
    void malformedNumberBecomesPackageNotFoundAndLegacySystemIsNotCalled() {
        assertThrows(PackageNotFoundException.class, () -> adapter.fetch("LP-abc"));
        assertThrows(PackageNotFoundException.class, () -> adapter.fetch("FP-1001"));
        assertThrows(PackageNotFoundException.class, () -> adapter.fetch(null));

        verifyNoInteractions(legacy);
    }
}
