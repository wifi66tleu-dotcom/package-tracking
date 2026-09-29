package com.tracking.registry;

import com.tracking.carrier.CarrierService;
import com.tracking.exception.UnknownCarrierException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CarrierRegistryTest {

    private final CarrierService first = mock(CarrierService.class);
    private final CarrierService second = mock(CarrierService.class);
    private final CarrierRegistry registry = new CarrierRegistry();

    CarrierRegistryTest() {
        registry.register("AA", first);
        registry.register("BB", second);
    }

    @Test
    void choosesCarrierFromTrackingNumberPrefix() throws Exception {
        assertSame(first, registry.resolve("AA-1"));
        assertSame(second, registry.resolve("BB-2"));
        assertSame(second, registry.resolve("bb-2"));
    }

    @Test
    void unknownPrefixOrBadFormatIsRejected() {
        assertThrows(UnknownCarrierException.class, () -> registry.resolve("ZZ-1"));
        assertThrows(UnknownCarrierException.class, () -> registry.resolve("1234"));
        assertThrows(UnknownCarrierException.class, () -> registry.resolve(null));
    }
}
