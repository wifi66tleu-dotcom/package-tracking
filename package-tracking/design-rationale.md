# Design Rationale — Package Tracking System

## 1. Problem and structure

My project displays the status of the package using the tracking number of that package. The reason why I have chosen this particular example is that both the carrier and the format of reporting are independent options, and a user may require either of them.
-  The Bridge abstraction is PackageTracker. It stores a CarrierService reference. SummaryTracker produces one line with the carrier name, tracking number and status. DetailedTracker also includes the location and update time.
- The implementor interface is CarrierService, with getName() and fetch(String trackingNumber). Its three implementations are FastPostCarrier, GlobalExpressCarrier and LegacyPostalAdapter. The native carriers use local sample data; this project does not connect to real delivery services.


## 2. Why both patterns are needed

Bridge separates report classes from the carrier classes. Every report is free to use any class that provides the CarrierService interface. I don’t need to create different classes like FastPostSummaryTracker and GlobalExpressSummaryTracker.

LegacyPostalSystem could not have been connected to bridge alone, since this class does not support our interface. Adapter would have solved the compatibility issue on its own, but would not have helped us establish two separate hierarchies of classes necessary for our report and carrier objects.

The LegacyPostalSystem is a legacy component simulation within the system. The adapter does not modify the API of the component but wraps it. The tracker components do not import any legacy classes or legacy error codes.

## 3. What the adapter converts

Our API expects a tracking number as input in the form of a string and outputs TrackingInfo. The older function was queryParcel(int parcelId, String language). It gives back PostalRecord with status in text form, update time in epoch seconds, and failures in numeric codes.

For example, the adapter converts LP-3003 into the integer 3003 and calls queryParcel(3003, "en"). It converts ON_THE_WAY into DeliveryStatus.IN_TRANSIT and converts the timestamp into an Instant. The tracker receives the same result type as it receives from the native carriers.

Code 404 will be CarrierPackageNotFoundException, while 503 will be CarrierUnavailableException along with other error codes. Null response and unknown status will be CarrierUnavailableException. Invalid carrier name LP-abc will be CarrierPackageNotFoundException before invoking the legacy API. The error code is not part of the public exception message.

## 4. Complexity module and extension

One complexity module was selected: dynamic implementor selection. CarrierRegistry chooses the carrier at runtime based on the prefix that is found before the dash. The prefixes "FP-1001" chooses "FastPost," "GX-2002" chooses "GlobalExpress," and "LP-3003" chooses the adapter.

Main registers the carriers that are present but uses resolve(trackingNumber) to choose which carrier to use. The chosen carrier object is passed to the tracker's constructor.

A new report type can be added for PackageTracker. New carriers will implement CarrierService and will have new prefixes registered. The current trackers, carriers, and registry lookup mechanisms need no changes at all. Registration will be simply configuration on the client side.

## 5. Tests and limitations

Mockito is used for controlling the responses of the carrier and legacy objects in JUnit 5 test cases. Tracker tests ensure that the delegation takes place correctly. The tests for adapters verify the translation and failure. The registry tests ensure correct selection.

The first drawback is that various errors from legacy code are placed in CarrierUnavailableException, which prevents callers from knowing the reason for failure. Unknown legacy code status also requires changes in the mapping of the adapter. Finally, an invalid legacy code timestamp is not turned into a tracking exception in the existing implementation.