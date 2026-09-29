package com.tracking.registry;
import com.tracking.carrier.CarrierService;
import com.tracking.exception.UnknownCarrierException;

import java.util.HashMap;
import java.util.Map;
public class CarrierRegistry {

    private final Map<String, CarrierService> carriers = new HashMap<>();

    public void register(String prefix, CarrierService carrier) {

        carriers.put(prefix.toUpperCase(), carrier);
    }

    public CarrierService resolve(String trackingNumber) throws UnknownCarrierException {
        int dash = trackingNumber == null ? -1 : trackingNumber.indexOf('-');
        if (dash <= 0) {
            throw new UnknownCarrierException(trackingNumber);
        }
        CarrierService carrier = carriers.get(trackingNumber.substring(0, dash).toUpperCase());
        if (carrier == null) {
            throw new UnknownCarrierException(trackingNumber);
        }
        return carrier;
    }
}
