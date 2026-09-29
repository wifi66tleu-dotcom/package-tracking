package com.tracking.adapter;

import com.tracking.carrier.CarrierService;
import com.tracking.exception.CarrierUnavailableException;
import com.tracking.exception.PackageNotFoundException;
import com.tracking.exception.TrackingException;
import com.tracking.legacy.LegacyPostalSystem;
import com.tracking.legacy.PostalRecord;
import com.tracking.model.DeliveryStatus;
import com.tracking.model.TrackingInfo;

import java.time.Instant;
public class LegacyPostalAdapter implements CarrierService {

    private static final String NAME = "LegacyPostal";
    private static final String PREFIX = "LP-";
    private static final String LANGUAGE = "en";

    private final LegacyPostalSystem legacy;

    public LegacyPostalAdapter(LegacyPostalSystem legacy) {

        this.legacy = legacy;
    }

    @Override
    public String getName() {

        return NAME;
    }

    @Override
    public TrackingInfo fetch(String trackingNumber) throws TrackingException {
        int parcelId = parseParcelId(trackingNumber);

        PostalRecord record = legacy.queryParcel(parcelId, LANGUAGE);
        if (record == null) {
            throw new CarrierUnavailableException(NAME, "empty response");
        }
        return switch (record.getCode()) {
            case LegacyPostalSystem.OK -> toTrackingInfo(trackingNumber, record);
            case LegacyPostalSystem.NOT_FOUND -> throw new PackageNotFoundException(trackingNumber);
            default -> throw new CarrierUnavailableException(NAME, "carrier reported an error");
        };
    }

    private int parseParcelId(String trackingNumber) throws PackageNotFoundException {
        if (trackingNumber == null || !trackingNumber.startsWith(PREFIX)) {
            throw new PackageNotFoundException(trackingNumber);
        }
        try {
            return Integer.parseInt(trackingNumber.substring(PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new PackageNotFoundException(trackingNumber);
        }
    }

    private TrackingInfo toTrackingInfo(String trackingNumber, PostalRecord record)
            throws CarrierUnavailableException {
        return new TrackingInfo(
                trackingNumber,
                toStatus(record.getStatusText()),
                record.getCity(),
                Instant.ofEpochSecond(record.getUpdatedEpochSeconds()));
    }

    private DeliveryStatus toStatus(String legacyText) throws CarrierUnavailableException {
        if (legacyText == null) {
            throw new CarrierUnavailableException(NAME, "response without status");
        }
        return switch (legacyText) {
            case "ACCEPTED" -> DeliveryStatus.REGISTERED;
            case "ON_THE_WAY" -> DeliveryStatus.IN_TRANSIT;
            case "WITH_COURIER" -> DeliveryStatus.OUT_FOR_DELIVERY;
            case "HANDED_OVER" -> DeliveryStatus.DELIVERED;
            default -> throw new CarrierUnavailableException(NAME, "unrecognised status");
        };
    }
}
