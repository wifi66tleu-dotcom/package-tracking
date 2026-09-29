package com.tracking.exception;
public class PackageNotFoundException extends TrackingException {
    public PackageNotFoundException(String trackingNumber) {

        super("Package not found: " + trackingNumber);
    }
}
