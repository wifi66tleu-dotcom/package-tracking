package com.tracking.exception;
public class CarrierUnavailableException extends TrackingException {
    public CarrierUnavailableException(String carrierName, String reason) {
        super("Carrier " + carrierName + " is unavailable: " + reason);
    }
}
