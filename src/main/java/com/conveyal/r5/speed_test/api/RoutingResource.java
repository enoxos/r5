/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package com.conveyal.r5.speed_test.api;

import com.conveyal.r5.speed_test.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.conveyal.r5.profile.ProfileRequest;

/**
 * This class defines all the JAX-RS query parameters for a path search as fields, allowing them to 
 * be inherited by other REST resource classes (the trip planner and the Analyst WMS or tile 
 * resource). They will be properly included in API docs generated by Enunciate. This implies that
 * the concrete REST resource subclasses will be request-scoped rather than singleton-scoped.
 *
 * All defaults should be specified in the RoutingRequest, NOT as annotations on the query parameters.
 * JSON router configuration can then overwrite those built-in defaults, and only the fields of the resulting prototype
 * routing request for which query parameters are found are overwritten here. This establishes a priority chain:
 * RoutingRequest field initializers, then JSON router config, then query parameters.
 *
 * @author abyrd
 */
public abstract class RoutingResource { 

    private static final Logger LOG = LoggerFactory.getLogger(RoutingResource.class);

    /**
     * The routerId selects between several graphs on the same server. The routerId is pulled from
     * the path, not the query parameters. However, the class RoutingResource is not annotated with
     * a path because we don't want it to be instantiated as an endpoint. Instead, the {routerId}
     * path parameter should be included in the path annotations of all its subclasses.
     */
    @PathParam("routerId") 
    public String routerId;

    /** The start location -- either latitude, longitude pair in degrees or a Vertex
     *  label. For example, <code>40.714476,-74.005966</code> or
     *  <code>mtanyctsubway_A27_S</code>.  */
    @QueryParam("fromPlace")
    protected String fromPlace;

    /** The end location (see fromPlace for format). */
    @QueryParam("toPlace")
    protected String toPlace;

    /** An ordered list of intermediate locations to be visited (see the fromPlace for format). Parameter can be specified multiple times. */
    @QueryParam("intermediatePlaces")
    protected List<String> intermediatePlaces;

    /** The date that the trip should depart (or arrive, for requests where arriveBy is true). */
    @QueryParam("date")
    protected String date;
    
    /** The time that the trip should depart (or arrive, for requests where arriveBy is true). */
    @QueryParam("time")
    protected String time;
    
    /** Whether the trip should depart or arrive at the specified date and time. */
    @QueryParam("arriveBy")
    protected Boolean arriveBy;
    
    /** Whether the trip must be wheelchair accessible. */
    @QueryParam("wheelchair")
    protected Boolean wheelchair;

    /** The maximum distance (in meters) the user is willing to walk. Defaults to unlimited. */
    @QueryParam("maxWalkDistance")
    protected Double maxWalkDistance;

    /**
     * The maximum time (in seconds) of pre-transit travel when using drive-to-transit (park and
     * ride or kiss and ride). Defaults to unlimited.
     */
    @QueryParam("maxPreTransitTime")
    protected Integer maxPreTransitTime;

    /**
     * A multiplier for how bad walking is, compared to being in transit for equal lengths of time.
     * Defaults to 2. Empirically, values between 10 and 20 seem to correspond well to the concept
     * of not wanting to walk too much without asking for totally ridiculous itineraries, but this
     * observation should in no way be taken as scientific or definitive. Your mileage may vary.
     */
    @QueryParam("walkReluctance")
    protected Double walkReluctance;

    /** How much more reluctant is the user to walk on streets with car traffic allowed **/
    @QueryParam("walkOnStreetReluctance")
    protected Double walkOnStreetReluctance;

    /**
     * How much worse is waiting for a transit vehicle than being on a transit vehicle, as a
     * multiplier. The default value treats wait and on-vehicle time as the same.
     *
     * It may be tempting to set this higher than walkReluctance (as studies often find this kind of
     * preferences among riders) but the planner will take this literally and walk down a transit
     * line to avoid waiting at a stop. This used to be set less than 1 (0.95) which would make
     * waiting offboard preferable to waiting onboard in an interlined trip. That is also
     * undesirable.
     *
     * If we only tried the shortest possible transfer at each stop to neighboring stop patterns,
     * this problem could disappear.
     */
    @QueryParam("waitReluctance")
    protected Double waitReluctance;

    /** How much less bad is waiting at the beginning of the trip (replaces waitReluctance) */
    @QueryParam("waitAtBeginningFactor")
    protected Double waitAtBeginningFactor;

    /** The user's walking speed in meters/second. Defaults to approximately 3 MPH. */
    @QueryParam("walkSpeed")
    protected Double walkSpeed;

    /** The user's biking speed in meters/second. Defaults to approximately 11 MPH, or 9.5 for bikeshare. */
    @QueryParam("bikeSpeed")
    protected Double bikeSpeed;

    /** The time it takes the user to fetch their bike and park it again in seconds.
     *  Defaults to 0. */
    @QueryParam("bikeSwitchTime")
    protected Integer bikeSwitchTime;

    /** The cost of the user fetching their bike and parking it again.
     *  Defaults to 0. */
    @QueryParam("bikeSwitchCost")
    protected Integer bikeSwitchCost;

    /** For bike triangle routing, how much safety matters (range 0-1). */
    @QueryParam("triangleSafetyFactor")
    protected Double triangleSafetyFactor;
    
    /** For bike triangle routing, how much slope matters (range 0-1). */
    @QueryParam("triangleSlopeFactor")
    protected Double triangleSlopeFactor;
    
    /** For bike triangle routing, how much time matters (range 0-1). */            
    @QueryParam("triangleTimeFactor")
    protected Double triangleTimeFactor;

    /** The set of characteristics that the user wants to optimize for. @See OptimizeType */
    @QueryParam("optimize")
    protected OptimizeType optimize;
    
    /** The set of modes that a user is willing to use, with qualifiers stating whether vehicles should be parked, rented, etc. */
    @QueryParam("mode")
    protected QualifiedModeSet modes;

    /** The minimum time, in seconds, between successive trips on different vehicles.
     *  This is designed to allow for imperfect schedule adherence.  This is a minimum;
     *  transfers over longer distances might use a longer time. */
    @QueryParam("minTransferTime")
    protected Integer minTransferTime;

    /** The maximum number of possible itineraries to return. */
    @QueryParam("numItineraries")
    protected Integer numItineraries;

    /**
     * The list of preferred routes. The format is agency_[routename][_routeid], so TriMet_100 (100 is route short name)
     * or Trimet__42 (two underscores, 42 is the route internal ID).
     */
    @QueryParam("preferredRoutes")
    protected String preferredRoutes;

    /** Penalty added for using every route that is not preferred if user set any route as preferred, i.e. number of seconds that we are willing
     * to wait for preferred route. */
    @QueryParam("otherThanPreferredRoutesPenalty")
    protected Integer otherThanPreferredRoutesPenalty;
    
    /** The comma-separated list of preferred agencies. */
    @QueryParam("preferredAgencies")
    protected String preferredAgencies;
    
    /**
     * The list of unpreferred routes. The format is agency_[routename][_routeid], so TriMet_100 (100 is route short name) or Trimet__42 (two
     * underscores, 42 is the route internal ID).
     */
    @QueryParam("unpreferredRoutes")
    protected String unpreferredRoutes;
    
    /** The comma-separated list of unpreferred agencies. */
    @QueryParam("unpreferredAgencies")
    protected String unpreferredAgencies;

    /** Whether intermediate stops -- those that the itinerary passes in a vehicle, but 
     *  does not board or alight at -- should be returned in the response.  For example,
     *  on a Q train trip from Prospect Park to DeKalb Avenue, whether 7th Avenue and
     *  Atlantic Avenue should be included. */
    @QueryParam("showIntermediateStops")
    protected Boolean showIntermediateStops;

    /**
     * Prevents unnecessary transfers by adding a cost for boarding a vehicle. This is the cost that
     * is used when boarding while walking.
     */
    @QueryParam("walkBoardCost")
    protected Integer walkBoardCost;
    
    /**
     * Prevents unnecessary transfers by adding a cost for boarding a vehicle. This is the cost that
     * is used when boarding while cycling. This is usually higher that walkBoardCost.
     */
    @QueryParam("bikeBoardCost")
    protected Integer bikeBoardCost;
    
    /**
     * The comma-separated list of banned routes. The format is agency_[routename][_routeid], so TriMet_100 (100 is route short name) or Trimet__42
     * (two underscores, 42 is the route internal ID).
     */
    @QueryParam("bannedRoutes")
    protected String bannedRoutes;

    /**
     * Functions the same as bannnedRoutes, except only the listed routes are allowed.
     */

    @QueryParam("whiteListedRoutes")
    protected String whiteListedRoutes;
    
    /** The comma-separated list of banned agencies. */
    @QueryParam("bannedAgencies")
    protected String bannedAgencies;

    /**
     * Functions the same as banned agencies, except only the listed agencies are allowed.
     */

    @QueryParam("whiteListedAgencies")
    protected String whiteListedAgencies;
    
    /** The comma-separated list of banned trips.  The format is agency_trip[:stop*], so:
     * TriMet_24601 or TriMet_24601:0:1:2:17:18:19
     */
    @QueryParam("bannedTrips")
    protected String bannedTrips;

    /** A comma-separated list of banned stops. A stop is banned by ignoring its 
     * pre-board and pre-alight edges. This means the stop will be reachable via the
     * street network. Also, it is still possible to travel through the stop. Just
     * boarding and alighting is prohibited.
     * The format is agencyId_stopId, so: TriMet_2107
     */
    @QueryParam("bannedStops")
    protected String bannedStops;
    
    /** A comma-separated list of banned stops. A stop is banned by ignoring its 
     * pre-board and pre-alight edges. This means the stop will be reachable via the
     * street network. It is not possible to travel through the stop.
     * For example, this parameter can be used when a train station is destroyed, such
     * that no trains can drive through the station anymore.
     * The format is agencyId_stopId, so: TriMet_2107
     */
    @QueryParam("bannedStopsHard")
    protected String bannedStopsHard;
    
    /**
     * An additional penalty added to boardings after the first.  The value is in OTP's
     * internal weight units, which are roughly equivalent to seconds.  Set this to a high
     * value to discourage transfers.  Of course, transfers that save significant
     * time or walking will still be taken.
     */
    @QueryParam("transferPenalty")
    protected Integer transferPenalty;
    
    /**
     * An additional penalty added to boardings after the first when the transfer is not
     * preferred. Preferred transfers also include timed transfers. The value is in OTP's
     * internal weight units, which are roughly equivalent to seconds. Set this to a high
     * value to discourage transfers that are not preferred. Of course, transfers that save
     * significant time or walking will still be taken.
     * When no preferred or timed transfer is defined, this value is ignored.
     */
    @QueryParam("nonpreferredTransferPenalty")
    protected Integer nonpreferredTransferPenalty;
    
    /** The maximum number of transfers (that is, one plus the maximum number of boardings)
     *  that a trip will be allowed.  Larger values will slow performance, but could give
     *  better routes.  This is limited on the server side by the MAX_TRANSFERS value in
     *  org.opentripplanner.api.ws.Planner. */
    @QueryParam("maxTransfers")
    protected Integer maxTransfers;

    /** If true, goal direction is turned off and a full path tree is built (specify only once) */
    @QueryParam("batch")
    protected Boolean batch;

    /** A transit stop required to be the first stop in the search (AgencyId_StopId) */
    @QueryParam("startTransitStopId")
    protected String startTransitStopId;

    /** A transit trip acting as a starting "state" for depart-onboard routing (AgencyId_TripId) */
    @QueryParam("startTransitTripId")
    protected String startTransitTripId;

    /**
     * When subtracting initial wait time, do not subtract more than this value, to prevent overly
     * optimistic trips. Reasoning is that it is reasonable to delay a trip start 15 minutes to 
     * make a better trip, but that it is not reasonable to delay a trip start 15 hours; if that
     * is to be done, the time needs to be included in the trip time. This number depends on the
     * transit system; for transit systems where trips are planned around the vehicles, this number
     * can be much higher. For instance, it's perfectly reasonable to delay one's trip 12 hours if
     * one is taking a cross-country Amtrak train from Emeryville to Chicago. Has no effect in
     * stock OTP, only in Analyst.
     *
     * A value of 0 means that initial wait time will not be subtracted out (will be clamped to 0).
     * A value of -1 (the default) means that clamping is disabled, so any amount of initial wait 
     * time will be subtracted out.
     */
    @QueryParam("clampInitialWait")
    protected Long clampInitialWait;

    /**
     * If true, this trip will be reverse-optimized on the fly. Otherwise, reverse-optimization
     * will occur once a trip has been chosen (in Analyst, it will not be done at all).
     */
    @QueryParam("reverseOptimizeOnTheFly")
    protected Boolean reverseOptimizeOnTheFly;
        
    @QueryParam("boardSlack")
    private Integer boardSlack;
    
    @QueryParam("alightSlack")
    private Integer alightSlack;

    @QueryParam("locale")
    private String locale;

    /**
     * If true, realtime updates are ignored during this search.
     */
    @QueryParam("ignoreRealtimeUpdates")
    protected Boolean ignoreRealtimeUpdates;

    /**
     * If true, the remaining weight heuristic is disabled. Currently only implemented for the long
     * distance path service.
     */
    @QueryParam("disableRemainingWeightHeuristic")
    protected Boolean disableRemainingWeightHeuristic;

    @QueryParam("maxHours")
    private Double maxHours;

    @QueryParam("useRequestedDateTimeInMaxHours")
    private Boolean useRequestedDateTimeInMaxHours;

    @QueryParam("disableAlertFiltering")
    private Boolean disableAlertFiltering;

    /**
     * If true, the Graph's ellipsoidToGeoidDifference is applied to all elevations returned by this query.
     */
    @QueryParam("geoidElevation")
    private Boolean geoidElevation;

    @QueryParam("heuristicStepsPerMainStep")
    private Integer heuristicStepsPerMainStep;

    /**
     * Range/sanity check the query parameter fields and build a Request object from them.
     *
     * @throws ParameterException when there is a problem interpreting a query parameter
     */
    protected ProfileRequest buildRequest() throws ParameterException {
        /*
        Router router = otpServer.getRouter(routerId);
        ProfileRequest request = new ProfileRequest();
        request.routerId = routerId;
        // The routing request should already contain defaults, which are set when it is initialized or in the JSON
        // router configuration and cloned. We check whether each parameter was supplied before overwriting the default.
        if (fromPlace != null)
            request.setFromString(fromPlace);

        if (toPlace != null)
            request.setToString(toPlace);

        request.parseTime(router.graph.getTimeZone(), this.date, this.time);

        if (wheelchair != null)
            request.setWheelchairAccessible(wheelchair);

        if (numItineraries != null)
            request.setNumItineraries(numItineraries);

        if (maxWalkDistance != null) {
            request.setMaxWalkDistance(maxWalkDistance);
            request.maxTransferWalkDistance = maxWalkDistance;
        }

        if (maxPreTransitTime != null)
            request.setMaxPreTransitTime(maxPreTransitTime);

        if (walkReluctance != null)
            request.setWalkReluctance(walkReluctance);

        if (waitReluctance != null)
            request.setWaitReluctance(waitReluctance);

        if (walkOnStreetReluctance != null)
            request.setWalkOnStreetReluctance(walkOnStreetReluctance);

        if (waitAtBeginningFactor != null)
            request.setWaitAtBeginningFactor(waitAtBeginningFactor);

        if (walkSpeed != null)
            request.walkSpeed = walkSpeed;

        if (bikeSpeed != null)
            request.bikeSpeed = bikeSpeed;

        if (bikeSwitchTime != null)
            request.bikeSwitchTime = bikeSwitchTime;

        if (bikeSwitchCost != null)
            request.bikeSwitchCost = bikeSwitchCost;

        if (optimize != null) {
            // Optimize types are basically combined presets of routing parameters, except for triangle
            request.setOptimize(optimize);
            if (optimize == OptimizeType.TRIANGLE) {
                RoutingRequest.assertTriangleParameters(triangleSafetyFactor, triangleTimeFactor, triangleSlopeFactor);
                request.setTriangleSafetyFactor(this.triangleSafetyFactor);
                request.setTriangleSlopeFactor(this.triangleSlopeFactor);
                request.setTriangleTimeFactor(this.triangleTimeFactor);
            }
        }

        if (arriveBy != null)
            request.setArriveBy(arriveBy);

        if (showIntermediateStops != null)
            request.showIntermediateStops = showIntermediateStops;

        if (intermediatePlaces != null)
            request.setIntermediatePlacesFromStrings(intermediatePlaces);

        if (preferredRoutes != null)
            request.setPreferredRoutes(preferredRoutes);

        if (otherThanPreferredRoutesPenalty != null)
            request.setOtherThanPreferredRoutesPenalty(otherThanPreferredRoutesPenalty);

        if (preferredAgencies != null)
            request.setPreferredAgencies(preferredAgencies);

        if (unpreferredRoutes != null)
            request.setUnpreferredRoutes(unpreferredRoutes);

        if (unpreferredAgencies != null)
            request.setUnpreferredAgencies(unpreferredAgencies);

        if (walkBoardCost != null)
            request.setWalkBoardCost(walkBoardCost);

        if (bikeBoardCost != null)
            request.setBikeBoardCost(bikeBoardCost);

        if (bannedRoutes != null)
            request.setBannedRoutes(bannedRoutes);

        if (whiteListedRoutes != null)
            request.setWhiteListedRoutes(whiteListedRoutes);

        if (bannedAgencies != null)
            request.setBannedAgencies(bannedAgencies);

        if (whiteListedAgencies != null)
            request.setWhiteListedAgencies(whiteListedAgencies);

        HashMap<AgencyAndId, BannedStopSet> bannedTripMap = makeBannedTripMap(bannedTrips);
        if (bannedTripMap != null)
            request.bannedTrips = bannedTripMap;

        if (bannedStops != null)
            request.setBannedStops(bannedStops);

        if (bannedStopsHard != null)
            request.setBannedStopsHard(bannedStopsHard);
        
        // The "Least transfers" optimization is accomplished via an increased transfer penalty.
        // See comment on RoutingRequest.transferPentalty.
        if (transferPenalty != null) request.transferPenalty = transferPenalty;
        if (optimize == OptimizeType.TRANSFERS) {
            optimize = OptimizeType.QUICK;
            request.transferPenalty += 1800;
        }

        if (batch != null)
            request.batch = batch;

        if (optimize != null)
            request.setOptimize(optimize);

        if (modes != null) {
            modes.applyToRoutingRequest(request);
            request.setModes(request.modes);
        }

        if (request.allowBikeRental && bikeSpeed == null) {
            //slower bike speed for bike sharing, based on empirical evidence from DC.
            request.bikeSpeed = 4.3;
        }

        if (boardSlack != null)
            request.boardSlack = boardSlack;

        if (alightSlack != null)
            request.alightSlack = alightSlack;

        if (minTransferTime != null)
            request.transferSlack = minTransferTime; // TODO rename field in routingrequest

        if (nonpreferredTransferPenalty != null)
            request.nonpreferredTransferPenalty = nonpreferredTransferPenalty;

        request.assertSlack();

        if (maxTransfers != null)
            request.maxTransfers = maxTransfers;

        final long NOW_THRESHOLD_MILLIS = 15 * 60 * 60 * 1000;
        boolean tripPlannedForNow = Math.abs(request.getDateTime().getTime() - new Date().getTime()) < NOW_THRESHOLD_MILLIS;
        request.useBikeRentalAvailabilityInformation = (tripPlannedForNow); // TODO the same thing for GTFS-RT

        if (startTransitStopId != null && !startTransitStopId.isEmpty())
            request.startingTransitStopId = AgencyAndId.convertFromString(startTransitStopId);

        if (startTransitTripId != null && !startTransitTripId.isEmpty())
            request.startingTransitTripId = AgencyAndId.convertFromString(startTransitTripId);

        if (clampInitialWait != null)
            request.clampInitialWait = clampInitialWait;

        if (reverseOptimizeOnTheFly != null)
            request.reverseOptimizeOnTheFly = reverseOptimizeOnTheFly;

        if (ignoreRealtimeUpdates != null)
            request.ignoreRealtimeUpdates = ignoreRealtimeUpdates;

        if (disableRemainingWeightHeuristic != null)
            request.disableRemainingWeightHeuristic = disableRemainingWeightHeuristic;

        if (maxHours != null)
            request.maxHours = maxHours;

        if (useRequestedDateTimeInMaxHours != null)
            request.useRequestedDateTimeInMaxHours = useRequestedDateTimeInMaxHours;

        if (disableAlertFiltering != null)
            request.disableAlertFiltering = disableAlertFiltering;

        if (geoidElevation != null)
            request.geoidElevation = geoidElevation;

        if (heuristicStepsPerMainStep != null)
            request.heuristicStepsPerMainStep = heuristicStepsPerMainStep;

        //getLocale function returns defaultLocale if locale is null
        request.locale = ResourceBundleSingleton.INSTANCE.getLocale(locale);
        */
        return null;
    }
}
