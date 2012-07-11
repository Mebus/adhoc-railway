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

import java.util.HashSet;
import java.util.Set;

import ch.fork.AdHocRailway.domain.routes.RouteItem;

import com.jgoodies.binding.beans.Model;

import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public class Turnout extends Model implements java.io.Serializable,
		Comparable<Turnout> {

	private int id;

	private TurnoutType turnoutType;

	private TurnoutGroup turnoutGroup;

	private int number;

	private String description;

	private String defaultState;

	private String orientation;

	private Set<RouteItem> routeItems = new HashSet<RouteItem>(0);

	private int address1;

	private int address2;

	private int bus1;

	private int bus2;

	private boolean address1Switched;

	private boolean address2Switched;

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_TURNOUT_TYPE = "turnoutType";
	public static final String PROPERTYNAME_TURNOUT_GROUP = "turnoutGroup";
	public static final String PROPERTYNAME_NUMBER = "number";
	public static final String PROPERTYNAME_DESCRIPTION = "description";
	public static final String PROPERTYNAME_DEFAULT_STATE = "defaultStateEnum";
	public static final String PROPERTYNAME_ORIENTATION = "orientationEnum";
	public static final String PROPERTYNAME_ROUTE_ITEMS = "routeItems";
	public static final String PROPERTYNAME_ADDRESS1 = "address1";
	public static final String PROPERTYNAME_ADDRESS2 = "address2";
	public static final String PROPERTYNAME_BUS1 = "bus1";
	public static final String PROPERTYNAME_BUS2 = "bus2";
	public static final String PROPERTYNAME_ADDRESS1_SWITCHED = "address1Switched";
	public static final String PROPERTYNAME_ADDRESS2_SWITCHED = "address2Switched";

	private int turnoutGroupId;

	private int turnoutTypeId;

	public enum TurnoutOrientation {
		NORTH, SOUTH, WEST, EAST
	};

	public TurnoutOrientation getOrientationEnum() {
		if (getOrientation().toUpperCase().equals("NORTH")) {
			return TurnoutOrientation.NORTH;
		} else if (getOrientation().toUpperCase().equals("SOUTH")) {
			return TurnoutOrientation.SOUTH;
		} else if (getOrientation().toUpperCase().equals("EAST")) {
			return TurnoutOrientation.EAST;
		} else if (getOrientation().toUpperCase().equals("WEST")) {
			return TurnoutOrientation.WEST;
		}
		return null;
	}

	public void setOrientationEnum(TurnoutOrientation orientation) {
		switch (orientation) {
		case NORTH:
			setOrientation("NORTH");
			break;
		case SOUTH:
			setOrientation("SOUTH");
			break;
		case WEST:
			setOrientation("WEST");
			break;
		case EAST:
			setOrientation("EAST");
			break;
		}
	}

	public SRCPTurnoutState getDefaultStateEnum() {
		if (getDefaultState().toUpperCase().equals("STRAIGHT")) {
			return SRCPTurnoutState.STRAIGHT;
		} else if (getDefaultState().toUpperCase().equals("LEFT")) {
			return SRCPTurnoutState.LEFT;
		} else if (getDefaultState().toUpperCase().equals("RIGHT")) {
			return SRCPTurnoutState.RIGHT;
		}
		return SRCPTurnoutState.UNDEF;
	}

	public void setDefaultStateEnum(SRCPTurnoutState state) {
		switch (state) {
		case STRAIGHT:
			setDefaultState("STRAIGHT");
			break;
		case LEFT:
			setDefaultState("LEFT");
			break;
		case RIGHT:
			setDefaultState("RIGHT");
			break;
		default:
			setDefaultState("UNDEF");
		}
	}

	public boolean isDefault() {
		return getTurnoutType().getTurnoutTypeEnum() == SRCPTurnoutTypes.DEFAULT;
	}

	public boolean isDoubleCross() {
		return getTurnoutType().getTurnoutTypeEnum() == SRCPTurnoutTypes.DOUBLECROSS;
	}

	public boolean isThreeWay() {
		return getTurnoutType().getTurnoutTypeEnum() == SRCPTurnoutTypes.THREEWAY;
	}

	public boolean isCutter() {
		return getTurnoutType().getTurnoutTypeEnum() == SRCPTurnoutTypes.CUTTER;
	}

	@Override
	public int compareTo(Turnout o) {
		if (this == o)
			return 0;
		if (o == null)
			return -1;
		if (number > o.getNumber())
			return 1;
		else if (number == o.getNumber())
			return 0;
		else
			return -1;
	}

	@Override
	public String toString() {
		String str = "#" + number;
		str += " Addr1 [" + bus1 + "," + address1 + "," + address1Switched
				+ "]";
		str += " Addr2 [" + bus2 + "," + address2 + "," + address2Switched
				+ "]";
		str += " default " + defaultState;
		str += " group " + turnoutGroup.getName();
		return str;
	}

	public Turnout() {
	}

	public Turnout(int id, TurnoutType turnoutType, TurnoutGroup turnoutGroup,
			int number, String description, String defaultState,
			String orientation, int address1, int bus1,
			boolean address1_switched) {
		this.id = id;
		this.turnoutType = turnoutType;
		this.turnoutGroup = turnoutGroup;
		this.number = number;
		this.description = description;
		this.defaultState = defaultState;
		this.orientation = orientation;
		this.address1 = address1;
		this.bus1 = bus1;
		this.address1Switched = address1_switched;
	}

	public Turnout(int id, TurnoutType turnoutType, TurnoutGroup turnoutGroup,
			int number, String description, String defaultState,
			String orientation, Set<RouteItem> routeItems, int address1,
			int address2, int bus1, int bus2, boolean address1_switched,
			boolean address2_switched) {
		this.id = id;
		this.turnoutType = turnoutType;
		this.turnoutGroup = turnoutGroup;
		this.number = number;
		this.description = description;
		this.defaultState = defaultState;
		this.orientation = orientation;
		this.routeItems = routeItems;
		this.address1 = address1;
		this.address2 = address2;
		this.bus1 = bus1;
		this.bus2 = bus2;
		this.address1Switched = address1_switched;
		this.address2Switched = address2_switched;

	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TurnoutType getTurnoutType() {
		return this.turnoutType;
	}

	public void setTurnoutType(TurnoutType turnoutType) {
		TurnoutType old = this.turnoutType;
		this.turnoutType = turnoutType;
		firePropertyChange(PROPERTYNAME_TURNOUT_TYPE, old, turnoutType);
	}

	public TurnoutGroup getTurnoutGroup() {
		return this.turnoutGroup;
	}

	public void setTurnoutGroup(TurnoutGroup turnoutGroup) {
		TurnoutGroup old = this.turnoutGroup;
		this.turnoutGroup = turnoutGroup;
		firePropertyChange(PROPERTYNAME_TURNOUT_GROUP, old, turnoutGroup);
	}

	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		int old = this.number;
		this.number = number;
		firePropertyChange(PROPERTYNAME_NUMBER, old, number);
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		String old = this.description;
		this.description = description;
		firePropertyChange(PROPERTYNAME_DESCRIPTION, old, description);
	}

	public String getDefaultState() {
		return this.defaultState;
	}

	public void setDefaultState(String defaultState) {
		String old = this.defaultState;
		this.defaultState = defaultState;
		firePropertyChange(PROPERTYNAME_DEFAULT_STATE, old, defaultState);
	}

	public String getOrientation() {
		return this.orientation;
	}

	public void setOrientation(String orientation) {
		String old = this.orientation;
		this.orientation = orientation;
		firePropertyChange(PROPERTYNAME_ORIENTATION, old, orientation);
	}

	public Set<RouteItem> getRouteItems() {
		return this.routeItems;
	}

	public void setRouteItems(Set<RouteItem> routeItems) {
		this.routeItems = routeItems;
	}

	public int getAddress1() {
		return this.address1;
	}

	public void setAddress1(int address1) {
		int old = this.address1;
		this.address1 = address1;
		firePropertyChange(PROPERTYNAME_ADDRESS1, old, address1);
	}

	public int getAddress2() {
		return this.address2;
	}

	public void setAddress2(int address2) {
		int old = this.address2;
		this.address2 = address2;
		firePropertyChange(PROPERTYNAME_ADDRESS2, old, address2);
	}

	public int getBus1() {
		return this.bus1;
	}

	public void setBus1(int bus1) {
		int old = this.bus1;
		this.bus1 = bus1;
		firePropertyChange(PROPERTYNAME_BUS1, old, bus1);
	}

	public int getBus2() {
		return this.bus2;
	}

	public void setBus2(int bus2) {
		int old = this.bus2;
		this.bus2 = bus2;
		firePropertyChange(PROPERTYNAME_BUS2, old, bus2);
	}

	public boolean isAddress1Switched() {
		return this.address1Switched;
	}

	public void setAddress1Switched(boolean address1Switched) {
		boolean old = this.address1Switched;
		this.address1Switched = address1Switched;
		firePropertyChange(PROPERTYNAME_ADDRESS1_SWITCHED, old,
				address1Switched);
	}

	public boolean isAddress2Switched() {
		return this.address2Switched;
	}

	public void setAddress2Switched(boolean address2Switched) {
		boolean old = this.address2Switched;
		this.address2Switched = address2Switched;
		firePropertyChange(PROPERTYNAME_ADDRESS2_SWITCHED, old,
				address2Switched);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address1;
		result = prime * result + (address1Switched ? 1231 : 1237);
		result = prime * result + address2;
		result = prime * result + (address2Switched ? 1231 : 1237);
		result = prime * result + bus1;
		result = prime * result + bus2;
		result = prime * result
				+ ((defaultState == null) ? 0 : defaultState.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + number;
		result = prime * result
				+ ((orientation == null) ? 0 : orientation.hashCode());
		result = prime * result
				+ ((turnoutGroup == null) ? 0 : turnoutGroup.hashCode());
		result = prime * result
				+ ((turnoutType == null) ? 0 : turnoutType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Turnout other = (Turnout) obj;
		if (address1 != other.address1)
			return false;
		if (address1Switched != other.address1Switched)
			return false;
		if (address2 != other.address2)
			return false;
		if (address2Switched != other.address2Switched)
			return false;
		if (bus1 != other.bus1)
			return false;
		if (bus2 != other.bus2)
			return false;
		if (defaultState == null) {
			if (other.defaultState != null)
				return false;
		} else if (!defaultState.equals(other.defaultState))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (number != other.number)
			return false;
		if (orientation == null) {
			if (other.orientation != null)
				return false;
		} else if (!orientation.equals(other.orientation))
			return false;
		if (turnoutGroup == null) {
			if (other.turnoutGroup != null)
				return false;
		} else if (!turnoutGroup.equals(other.turnoutGroup))
			return false;
		if (turnoutType == null) {
			if (other.turnoutType != null)
				return false;
		} else if (!turnoutType.equals(other.turnoutType))
			return false;
		return true;
	}

	@Override
	public Object clone() {
		Turnout newT = new Turnout(id, turnoutType, turnoutGroup, number,
				description, defaultState, orientation, routeItems, address1,
				address2, bus1, bus2, address1Switched, address2Switched);
		return newT;
	}

	public void setTurnoutGroupId(int turnoutGroupId) {
		this.turnoutGroupId = turnoutGroupId;

	}

	public int getTurnoutGroupId() {
		return turnoutGroupId;
	}

	public int getTurnoutTypeId() {
		return turnoutTypeId;
	}

	public void setTurnoutTypeId(int turnoutTypeId) {
		this.turnoutTypeId = turnoutTypeId;

	}

}
