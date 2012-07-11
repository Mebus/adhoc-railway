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

package ch.fork.AdHocRailway.services.routes;

import java.util.List;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceException;

public class XMLRouteService implements RouteService {
	private static Logger logger = Logger.getLogger(XMLRouteService.class);
	private static XMLRouteService instance;

	private XMLRouteService() {
		logger.info("XMLRoutePersistence loaded");
	}

	public static XMLRouteService getInstance() {
		if (instance == null) {
			instance = new XMLRouteService();
		}
		return instance;
	}

	@Override
	public List<Route> getAllRoutes() throws RoutePersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addRoute(Route route) throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteRoute(Route route) throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateRoute(Route route) throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<RouteGroup> getAllRouteGroups()
			throws RoutePersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addRouteItem(RouteItem item) throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteRouteItem(RouteItem item)
			throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateRouteItem(RouteItem item)
			throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() throws RoutePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<RouteItem> getAllRouteItems() throws RoutePersistenceException {
		// TODO Auto-generated method stub
		return null;
	}
}