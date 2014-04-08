/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteItem.java 308 2013-05-01 15:43:50Z fork_ch $
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

import ch.fork.AdHocRailway.domain.AbstractItem;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RouteItem extends AbstractItem implements java.io.Serializable,
        Comparable<RouteItem> {

    public static final String PROPERTYNAME_ID = "id";
    public static final String PROPERTYNAME_TURNOUT = "turnout";
    public static final String PROPERTYNAME_ROUTE = "route";
    public static final String PROPERTYNAME_ROUTED_STATE = "state";
    @Expose
    private String id;
    @Expose
    private String turnoutId;
    @Expose
    private TurnoutState state;

    private transient Turnout turnout;

    private transient Route route;


    public RouteItem() {
    }

    public RouteItem(final String id, final Turnout turnout, final Route route,
                     final TurnoutState routedState) {
        this.id = id;
        this.turnout = turnout;
        this.route = route;
        this.state = routedState;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Turnout getTurnout() {
        return this.turnout;
    }

    public void setTurnout(final Turnout turnout) {
        final Turnout old = this.turnout;
        this.turnout = turnout;
        setTurnoutId(turnout.getId());
        changeSupport.firePropertyChange(PROPERTYNAME_TURNOUT, old,
                this.turnout);
    }

    public Route getRoute() {
        return this.route;
    }

    public void setRoute(final Route route) {
        final Route old = this.route;
        this.route = route;
        changeSupport.firePropertyChange(PROPERTYNAME_ROUTE, old, this.route);
    }

    public TurnoutState getState() {
        return this.state;
    }

    public void setState(final TurnoutState state) {
        final TurnoutState old = this.state;
        this.state = state;
        changeSupport.firePropertyChange(PROPERTYNAME_ROUTED_STATE, old,
                this.state);
    }

    @Override
    public int compareTo(final RouteItem o) {
        return id.compareTo(o.getId());
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getTurnoutId() {
        return turnoutId;
    }

    public void setTurnoutId(String turnoutId) {
        this.turnoutId = turnoutId;
    }
}
