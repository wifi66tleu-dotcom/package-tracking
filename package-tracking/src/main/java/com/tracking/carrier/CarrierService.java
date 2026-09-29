package com.tracking.carrier;

import com.tracking.exception.TrackingException;
import com.tracking.model.TrackingInfo;

public interface CarrierService {

    String getName();

    TrackingInfo fetch(String trackingNumber) throws TrackingException;
}
