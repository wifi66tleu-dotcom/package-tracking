package com.tracking.model;
import java.time.Instant;
public record TrackingInfo(String trackingNumber,
                           DeliveryStatus status,
                           String location,
                           Instant updatedAt) {
}
