package com.tracking.exception;
public class UnknownCarrierException extends TrackingException {
    public UnknownCarrierException(String trackingNumber) {
        super("No carrier found for tracking number: " + trackingNumber);
    }
}
