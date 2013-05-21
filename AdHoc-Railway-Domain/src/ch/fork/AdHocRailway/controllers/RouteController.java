/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteControlIface.java 279 2013-04-02 20:46:41Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.controllers.impl.brain.BrainRouteControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;

public abstract class RouteController {

	public abstract void enableRoute(Route r) throws RouteException;

	public abstract void disableRoute(Route r) throws RouteException;

	public abstract boolean isRouteEnabled(Route route);

	public abstract boolean isRouting(Route route);

	public abstract void toggle(Route route) throws RouteException;

	public abstract void toggleTest(Route route) throws RouteException;

	public abstract void addOrUpdateRoute(Route route);

	public abstract void addRouteChangeListener(Route r,
			RouteChangeListener listener);

	public abstract void removeAllRouteChangeListeners();

	public abstract void removeRouteChangeListener(Route r,
			RouteChangeListener listener);

	public static RouteController createLocomotiveController(
			final RailwayDevice railwayDevice,
			final TurnoutController turnoutController) {
		switch (railwayDevice) {
		case ADHOC_BRAIN:
			return new BrainRouteControlAdapter();
		case SRCP:
			return new SRCPRouteControlAdapter(
					(SRCPTurnoutControlAdapter) turnoutController);
		default:
			throw new IllegalArgumentException("unknown railway-device"
					+ railwayDevice);

		}
	}

	public abstract void setRoutingDelay(final int intValue);
}