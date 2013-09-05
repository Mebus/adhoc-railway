/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryTurnoutPersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

package ch.fork.AdHocRailway.manager.impl.turnouts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerListener;
import ch.fork.AdHocRailway.services.turnouts.TurnoutService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutServiceListener;

public class TurnoutManagerImpl implements TurnoutManager,
		TurnoutServiceListener {
	private static final Logger LOGGER = Logger
			.getLogger(TurnoutManagerImpl.class);

	private final Map<Integer, Turnout> numberToTurnoutCache = new HashMap<Integer, Turnout>();

	private TurnoutService turnoutService;

	private final SortedSet<TurnoutGroup> turnoutGroups = new TreeSet<TurnoutGroup>();

	private TurnoutController turnoutControl = null;

	private final Set<TurnoutManagerListener> listeners = new HashSet<TurnoutManagerListener>();

	private final Set<TurnoutManagerListener> listenersToBeRemovedInNextEvent = new HashSet<TurnoutManagerListener>();

	private int lastProgrammedAddress = 1;

	private int lastProgrammedNumber = 0;

	public TurnoutManagerImpl() {
		LOGGER.info("TurnoutManagerImpl loaded");
	}

	@Override
	public void addTurnoutManagerListener(final TurnoutManagerListener listener) {
		this.listeners.add(listener);
		listener.turnoutsUpdated(turnoutGroups);
	}

	@Override
	public void removeTurnoutManagerListenerInNextEvent(
			final TurnoutManagerListener turnoutAddListener) {
		listenersToBeRemovedInNextEvent.add(turnoutAddListener);
	}

	private void cleanupListeners() {
		listeners.removeAll(listenersToBeRemovedInNextEvent);
		listenersToBeRemovedInNextEvent.clear();
	}

	@Override
	public void clear() {
		LOGGER.debug("clear()");
		clearCache();
		turnoutsUpdated(getAllTurnoutGroups());
	}

	@Override
	public void clearToService() {
		LOGGER.debug("clearToService()");
		turnoutService.clear();
	}

	@Override
	public List<Turnout> getAllTurnouts() {
		return new ArrayList<Turnout>(numberToTurnoutCache.values());
	}

	@Override
	public Turnout getTurnoutByNumber(final int number)
			throws TurnoutManagerException {
		LOGGER.debug("getTurnoutByNumber()");
		return numberToTurnoutCache.get(number);
	}

	@Override
	public void addTurnoutToGroup(final Turnout turnout,
			final TurnoutGroup group) throws TurnoutManagerException {
		LOGGER.debug("addTurnout()");
		if (group == null) {
			throw new TurnoutManagerException("Turnout has no associated Group");
		}
		group.getTurnouts().add(turnout);
		turnout.setTurnoutGroup(group);
		turnoutService.addTurnout(turnout);
		if (turnout.getTurnoutType().equals(TurnoutType.THREEWAY)) {
			lastProgrammedAddress = turnout.getAddress2();
		} else {
			lastProgrammedAddress = turnout.getAddress1();
		}
		lastProgrammedNumber = turnout.getNumber();

	}

	@Override
	public void removeTurnout(final Turnout turnout) {
		LOGGER.debug("removeTurnout(" + turnout + ")");
		turnoutService.removeTurnout(turnout);
		final TurnoutGroup group = turnout.getTurnoutGroup();
		group.getTurnouts().remove(turnout);

		final Set<RouteItem> routeItems = turnout.getRouteItems();
		for (final RouteItem ri : routeItems) {
			final Route route = ri.getRoute();
			route.getRouteItems().remove(ri);
		}
	}

	@Override
	public void updateTurnout(final Turnout turnout)
			throws TurnoutManagerException {
		LOGGER.debug("updateTurnout()");
		turnoutService.updateTurnout(turnout);
	}

	@Override
	public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
		return turnoutGroups;
	}

	@Override
	public TurnoutGroup getTurnoutGroupByName(final String name) {
		LOGGER.debug("getTurnoutGroupByName()");

		for (final TurnoutGroup group : turnoutGroups) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		return null;
	}

	@Override
	public void addTurnoutGroup(final TurnoutGroup group) {
		LOGGER.debug("addTurnoutGroup()");
		turnoutService.addTurnoutGroup(group);
	}

	@Override
	public void removeTurnoutGroup(final TurnoutGroup group)
			throws TurnoutManagerException {
		LOGGER.debug("removeTurnoutGroup()");
		if (!group.getTurnouts().isEmpty()) {
			throw new TurnoutManagerException(
					"Cannot delete Turnout-Group with associated Routes");
		}
		turnoutService.removeTurnoutGroup(group);
	}

	@Override
	public void updateTurnoutGroup(final TurnoutGroup group) {
		LOGGER.debug("updateTurnoutGroup()");
		turnoutService.updateTurnoutGroup(group);
	}

	@Override
	public int getNextFreeTurnoutNumber() {
		LOGGER.debug("getNextFreeTurnoutNumber()");

		if (lastProgrammedNumber == 0) {
			final SortedSet<Turnout> turnoutsNumbers = new TreeSet<Turnout>(
					new Comparator<Turnout>() {

						@Override
						public int compare(final Turnout o1, final Turnout o2) {
							return Integer.valueOf(o1.getNumber()).compareTo(
									Integer.valueOf(o2.getNumber()));
						}
					});
			turnoutsNumbers.addAll(getAllTurnouts());
			if (turnoutsNumbers.isEmpty()) {
				lastProgrammedNumber = 0;
			} else {
				lastProgrammedNumber = turnoutsNumbers.last().getNumber();
			}
		}

		return lastProgrammedNumber + 1;
	}

	@Override
	public boolean isTurnoutNumberFree(final int number) {
		return !numberToTurnoutCache.containsKey(number);
	}

	@Override
	public void setTurnoutService(final TurnoutService instance) {
		this.turnoutService = instance;
	}

	@Override
	public void initialize() {
		clear();
		cleanupListeners();

		turnoutService.init(this);
	}

	@Override
	public void setTurnoutControl(final TurnoutController turnoutControl) {
		this.turnoutControl = turnoutControl;
	}

	@Override
	public void turnoutsUpdated(final SortedSet<TurnoutGroup> updatedTurnouts) {
		LOGGER.info("turnoutsUpdated: " + updatedTurnouts);
		cleanupListeners();
		clearCache();
		for (final TurnoutGroup group : updatedTurnouts) {
			putTurnoutGroupInCache(group);
			for (final Turnout turnout : group.getTurnouts()) {
				numberToTurnoutCache.put(turnout.getNumber(), turnout);
				putInCache(turnout);
			}
		}
		for (final TurnoutManagerListener l : listeners) {
			l.turnoutsUpdated(this.turnoutGroups);
		}
	}

	@Override
	public void turnoutAdded(final Turnout turnout) {
		LOGGER.info("turnoutAdded: " + turnout);
		cleanupListeners();
		putInCache(turnout);
		for (final TurnoutManagerListener l : listeners) {
			l.turnoutAdded(turnout);
		}
	}

	@Override
	public void turnoutUpdated(final Turnout turnout) {
		LOGGER.info("turnoutUpdated: " + turnout);
		cleanupListeners();
		putInCache(turnout);
		for (final TurnoutManagerListener l : listeners) {
			l.turnoutUpdated(turnout);
		}
	}

	@Override
	public void turnoutRemoved(final Turnout turnout) {
		LOGGER.info("turnoutRemoved: " + turnout);
		cleanupListeners();
		removeFromCache(turnout);
		for (final TurnoutManagerListener l : listeners) {
			l.turnoutRemoved(turnout);
		}
	}

	@Override
	public void turnoutGroupAdded(final TurnoutGroup group) {
		LOGGER.info("turnoutGroupAdded: " + group);
		cleanupListeners();
		putTurnoutGroupInCache(group);
		for (final TurnoutManagerListener l : listeners) {
			l.turnoutGroupAdded(group);
		}
	}

	@Override
	public void turnoutGroupUpdated(final TurnoutGroup group) {
		LOGGER.info("turnoutGroupUpdated: " + group);
		cleanupListeners();
		removeTurnoutGroupFromCache(group);
		putTurnoutGroupInCache(group);
		for (final TurnoutManagerListener l : listeners) {
			l.turnoutGroupUpdated(group);
		}
	}

	@Override
	public void turnoutGroupRemoved(final TurnoutGroup group) {
		LOGGER.info("turnoutGroupDeleted: " + group);
		cleanupListeners();
		removeTurnoutGroupFromCache(group);
		for (final TurnoutManagerListener l : listeners) {
			l.turnoutGroupRemoved(group);
		}
	}

	@Override
	public void failure(final TurnoutManagerException arg0) {
		LOGGER.warn("failure", arg0);
		cleanupListeners();
		for (final TurnoutManagerListener l : listeners) {
			l.failure(arg0);
		}
	}

	@Override
	public void disconnect() {
		cleanupListeners();
		turnoutService.disconnect();
		turnoutsUpdated(new TreeSet<TurnoutGroup>());
	}

	@Override
	public int getLastProgrammedAddress() {
		return lastProgrammedAddress;
	}

	private void putTurnoutGroupInCache(final TurnoutGroup group) {
		turnoutGroups.add(group);
	}

	private void removeTurnoutGroupFromCache(final TurnoutGroup group) {
		turnoutGroups.remove(group);
	}

	private void putInCache(final Turnout turnout) {
		numberToTurnoutCache.put(turnout.getNumber(), turnout);
		turnoutControl.addOrUpdateTurnout(turnout);
	}

	private void removeFromCache(final Turnout turnout) {
		numberToTurnoutCache.values().remove(turnout);
	}

	private void clearCache() {
		this.numberToTurnoutCache.clear();
		this.turnoutGroups.clear();
	}

	@Override
	public TurnoutService getService() {
		return turnoutService;
	}
}
