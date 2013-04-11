/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryRoutePersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

package ch.fork.AdHocRailway.services.impl.xml;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;
import ch.fork.AdHocRailway.services.turnouts.RouteService;
import ch.fork.AdHocRailway.services.turnouts.RouteServiceListener;

public class XMLRouteService implements RouteService {
	private static Logger logger = Logger.getLogger(XMLRouteService.class);
	private final SortedSet<Route> routes = new TreeSet<Route>();
	private final SortedSet<RouteGroup> routeGroups = new TreeSet<RouteGroup>();
	private RouteServiceListener listener;

	public XMLRouteService() {
		logger.info("XMLRoutePersistence loaded");
	}

	@Override
	public void addRoute(final Route route) throws RouteManagerException {
		routes.add(route);
		listener.routeAdded(route);
	}

	@Override
	public void removeRoute(final Route route) throws RouteManagerException {
		routes.remove(route);
		listener.routeRemoved(route);
	}

	@Override
	public void updateRoute(final Route route) throws RouteManagerException {
		routes.remove(route);
		routes.add(route);
		listener.routeUpdated(route);

	}

	@Override
	public SortedSet<RouteGroup> getAllRouteGroups()
			throws RouteManagerException {
		return routeGroups;
	}

	@Override
	public void addRouteGroup(final RouteGroup routeGroup)
			throws RouteManagerException {
		routeGroups.add(routeGroup);
		listener.routeGroupAdded(routeGroup);
	}

	@Override
	public void removeRouteGroup(final RouteGroup routeGroup)
			throws RouteManagerException {
		routeGroups.remove(routeGroup);
		listener.routeGroupRemoved(routeGroup);

	}

	@Override
	public void updateRouteGroup(final RouteGroup routeGroup)
			throws RouteManagerException {
		routeGroups.remove(routeGroup);
		routeGroups.add(routeGroup);
		listener.routeGroupUpdated(routeGroup);

	}

	@Override
	public void addRouteItem(final RouteItem item) throws RouteManagerException {

	}

	@Override
	public void removeRouteItem(final RouteItem item)
			throws RouteManagerException {

	}

	@Override
	public void updateRouteItem(final RouteItem item)
			throws RouteManagerException {

	}

	@Override
	public void clear() throws RouteManagerException {
		routes.clear();
		routeGroups.clear();
	}

	@Override
	public void init(final RouteServiceListener listener) {
		this.listener = listener;
	}

	@Override
	public void disconnect() {

	}

	public void loadRouteGroupsFromXML(final SortedSet<RouteGroup> groups) {
		routeGroups.clear();
		routes.clear();
		if (groups != null) {
			for (final RouteGroup group : groups) {
				group.setId(UUID.randomUUID().hashCode());
				routeGroups.add(group);
				for (final Route turnout : group.getRoutes()) {
					turnout.setId(UUID.randomUUID().hashCode());
					routes.add(turnout);
				}
			}
		}
		listener.routesUpdated(routeGroups);
	}

}