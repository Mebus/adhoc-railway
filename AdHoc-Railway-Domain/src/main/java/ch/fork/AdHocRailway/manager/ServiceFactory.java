package ch.fork.AdHocRailway.manager;

import ch.fork.AdHocRailway.services.impl.socketio.locomotives.SIOLocomotiveService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIORouteService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIOTurnoutService;
import ch.fork.AdHocRailway.services.impl.xml.XMLLocomotiveService;
import ch.fork.AdHocRailway.services.impl.xml.XMLRouteService;
import ch.fork.AdHocRailway.services.impl.xml.XMLTurnoutService;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveService;
import ch.fork.AdHocRailway.services.turnouts.RouteService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutService;

public class ServiceFactory {

	public static LocomotiveService createLocomotiveService(
			final boolean useAdHocServer) {

		if (useAdHocServer) {
			return new SIOLocomotiveService();
		} else {
			return new XMLLocomotiveService();
		}
	}

	public static TurnoutService createTurnoutService(
			final boolean useAdHocServer) {
		if (useAdHocServer) {
			return new SIOTurnoutService();
		} else {
			return new XMLTurnoutService();
		}
	}

	public static RouteService createRouteService(final boolean useAdHocServer) {

		if (useAdHocServer) {
			return new SIORouteService();
		} else {
			return new XMLRouteService();
		}

	}
}
