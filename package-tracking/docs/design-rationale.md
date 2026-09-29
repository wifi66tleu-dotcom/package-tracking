# Design Rationale — Package Tracking System (Bridge + Adapter)

## 1. Problem Chosen

A customer types in a tracking number and wants to know where the package is. That sounds simple, but two things in this system change independently of each other:

- **How the answer is shown.** Sometimes a short one-line summary is enough. Sometimes the customer wants a full report with location and time. Later there could be more styles (for example a JSON output or an SMS text). This is the *abstraction* side: `PackageTracker` with `SummaryTracker` and `DetailedTracker`.
- **Which carrier gives the data.** Right now there are FastPost, GlobalExpress and an old legacy postal system. Later there could be more. This is the *implementation* side: `CarrierService` with `FastPostCarrier`, `GlobalExpressCarrier` and `LegacyPostalAdapter`.

The legacy postal system is an old third-party class. We cannot change it, so we pretend we only have its jar file.

The client code (`Main`) should not need to know which carrier is used. It only chooses how the result is shown. The carrier is picked automatically from the tracking number prefix (`FP-…`, `GX-…`, `LP-…`).

## 2. Why Bridge Alone Would Not Be Enough

Bridge works well when every implementation already fits the same interface. Here that interface is `CarrierService`, with one method: `fetch(String trackingNumber)`.

`LegacyPostalSystem` does not fit it. It has a different method name, different parameters and a different way of reporting errors. If I used only Bridge, I would have two bad options:

1. Edit the legacy class so it implements `CarrierService`. I can't do that, because it is third-party code.
2. Let `PackageTracker` know about the legacy system and call it in a special way. That breaks the main rule of Bridge: the abstraction should only know the implementor interface, nothing else.

Bridge lets the two sides change independently, but it does not make an incompatible class fit. That is why an Adapter is needed.

## 3. Why Adapter Alone Would Not Be Enough

An Adapter would solve the legacy problem: it would make `LegacyPostalSystem` look like a `CarrierService`. But without Bridge, I would still need a separate report class for every combination of style and carrier:

- 2 styles × 3 carriers = **6 classes**
- add one new carrier and one new style: 3 × 4 = **12 classes**

With Bridge the two sides are separate, so the numbers just add up instead of multiplying:

- 2 + 3 = **5 classes**
- add one new carrier and one new style: 3 + 4 = **7 classes**

In short: Adapter solves *compatibility*, Bridge solves *class explosion*. These are two different problems in the same system, so I use both.

## 4. Why the Wrapped Class Is Genuinely Incompatible

`LegacyPostalSystem` is not just "the same thing with another method name". It is different in almost every way:

| Aspect | `CarrierService` (our contract) | `LegacyPostalSystem` |
| --- | --- | --- |
| Method | `fetch(String)` | `queryParcel(int, String)` |
| Parameters | Tracking number as `String` | Parcel id as `int`, plus an extra `language` argument |
| Result | `TrackingInfo` (enum status, `Instant` time) | `PostalRecord` (status as text, time as epoch seconds) |
| Failure signal | Checked `TrackingException` classes | Error codes (0 / 404 / 503) inside the result, no exceptions |
| Status words | `DeliveryStatus` enum | Its own strings: `ACCEPTED`, `ON_THE_WAY`, `WITH_COURIER`, `HANDED_OVER` |

So `LegacyPostalAdapter` has real work to do:

- It turns `"LP-3003"` into the number `3003`.
- It adds the extra `language` argument itself (always `"en"`), so the rest of the system never has to care about it.
- It turns the status text into our enum: `ON_THE_WAY` becomes `IN_TRANSIT`, `HANDED_OVER` becomes `DELIVERED`, and so on.
- It turns epoch seconds into an `Instant`.
- It turns error codes into exceptions: code 404 becomes `PackageNotFoundException`. Code 503, any other code, an empty response, a missing status or an unknown status all become `CarrierUnavailableException`.
- If the tracking number is broken (for example `LP-abc`), it says "package not found" right away without even calling the legacy system.

So the adapter is not just renaming methods. It translates identifiers, types, meanings and the whole error style.

## 5. Failure Translation and Open/Closed Principle

Every carrier follows the same contract, written in `CarrierService`:

- success returns a non-null `TrackingInfo`;
- an unknown package throws `PackageNotFoundException`;
- a carrier that can't answer throws `CarrierUnavailableException`;
- nothing else is thrown, and `null` is never returned.

Because of this, the trackers never see legacy error codes. The error numbers (404, 503) are not even put into exception messages, so nothing legacy-specific leaks out. `PackageTracker` and its subclasses never import anything from `com.tracking.legacy`.

The design is open for extension on both sides without editing existing code:

- **New way to show results:** write a new subclass of `PackageTracker`. The test `OpenClosedPrincipleTest` does exactly this with a `ShoutingTracker`. No existing class changes.
- **New carrier:** write a class that implements `CarrierService` and add one line, for example `registry.register("DR", new DroneCarrier())`. `CarrierRegistry`, the trackers and the other carriers stay untouched. The same test shows this with a `DroneCarrier`.

## 6. Complexity Module: Dynamic Implementor Selection

The carrier is chosen **at runtime**, not written in the code by the client.

`CarrierRegistry` looks at the prefix of the tracking number, the part before the dash. `FP-1001` goes to FastPost, `GX-2002` goes to GlobalExpress and `LP-3003` goes to the legacy adapter. The adapted legacy carrier is selected in exactly the same way as the native ones, so from the outside nobody can tell the difference.

If the number has no dash, or the prefix is not registered, the registry throws `UnknownCarrierException`.

This means `Main` only says "I want a summary" or "I want a detailed report". It never says "use FastPost". Adding a new carrier is only one more `register(...)` call, and `CarrierRegistry` itself is never edited.

## 7. Why CarrierRegistry Is Outside the Bridge

`CarrierRegistry` is not part of the abstraction or the implementor hierarchy. It is a separate helper that sits next to them.

- It is not a carrier. It does not fetch any tracking data.
- It is not a way of presenting results either. It does not build any reports.
- Its only job is to find the right `CarrierService` for a tracking number.

If I put this logic inside `PackageTracker`, every tracker would have to know about prefixes and about all the carriers, and the abstraction would depend on details it should not know. If I put it inside the carriers, they would have to know about each other. Keeping it separate means the trackers only know the `CarrierService` interface, and the carriers only know how to answer for themselves. `Main` is the one place that knows all the concrete classes (the composition root), and that is where the carriers get registered.

## 8. Limitations of the Final Design

**Some legacy errors are merged into one.** The adapter treats every legacy failure except "not found" as `CarrierUnavailableException`. The legacy system has different codes, for example "service is down" and other error codes, but the caller cannot tell them apart and cannot react differently. Also, the adapter only knows the four legacy status texts that exist today. If the legacy system starts sending a new status, the adapter reports it as "unavailable" until someone updates the mapping.

**The `LP` prefix is written in two places.** The adapter itself checks that a number starts with `LP-`, and `Main` registers it in the registry under `"LP"`. If someone changed one and forgot the other, the legacy carrier would be selected but then reject every number as "not found". The two are not linked in code right now, so this must be kept in sync by hand.