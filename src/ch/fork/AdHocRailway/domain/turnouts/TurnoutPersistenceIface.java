/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
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

package ch.fork.AdHocRailway.domain.turnouts;

import java.util.Set;
import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;

import com.jgoodies.binding.list.ArrayListModel;

public interface TurnoutPersistenceIface {

	public abstract ArrayListModel<Turnout> getAllTurnouts();

	public abstract Turnout getTurnoutByNumber(int number)
			throws TurnoutPersistenceException;

	public abstract Turnout getTurnoutByAddressBus(int bus, int address);

	public abstract void addTurnout(Turnout turnout)
			throws TurnoutPersistenceException;

	public abstract void deleteTurnout(Turnout turnout)
			throws TurnoutPersistenceException;

	public abstract void updateTurnout(Turnout turnout)
			throws TurnoutPersistenceException;

	public abstract ArrayListModel<TurnoutGroup> getAllTurnoutGroups();

	public abstract TurnoutGroup getTurnoutGroupByName(String name);

	public abstract void addTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException;

	public abstract void deleteTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException;

	public abstract void updateTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException;

	public abstract SortedSet<TurnoutType> getAllTurnoutTypes();

	public abstract TurnoutType getTurnoutType(TurnoutTypes typeName);

	public abstract void addTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException;

	public abstract void deleteTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException;

	public int getNextFreeTurnoutNumber();

	public Set<Integer> getUsedTurnoutNumbers();

	public abstract void clear() throws TurnoutPersistenceException;

	public int getNextFreeTurnoutNumberOfGroup(TurnoutGroup turnoutGroup);
	
	public void enlargeTurnoutGroups();
	
	public abstract void flush();
}