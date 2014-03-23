/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutPersistenceIface.java 199 2012-01-14 23:46:24Z fork_ch $
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

package ch.fork.AdHocRailway.services;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

import java.util.SortedSet;

public interface TurnoutService {

    public abstract void init(TurnoutServiceListener listener);

    public abstract void addTurnout(Turnout turnout);

    public abstract void removeTurnout(Turnout turnout);

    public abstract void updateTurnout(Turnout turnout);

    public abstract SortedSet<TurnoutGroup> getAllTurnoutGroups();

    public abstract void addTurnoutGroup(TurnoutGroup group);

    public abstract void removeTurnoutGroup(TurnoutGroup group);

    public abstract void updateTurnoutGroup(TurnoutGroup group);

    public abstract void clear();

    public abstract void disconnect();
}