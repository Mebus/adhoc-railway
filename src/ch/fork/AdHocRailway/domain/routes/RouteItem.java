package ch.fork.AdHocRailway.domain.routes;

// Generated 08-Aug-2007 18:10:44 by Hibernate Tools 3.2.0.beta8

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;

import com.jgoodies.binding.beans.Model;

/**
 * RouteItem generated by hbm2java
 */
@Entity
@Table(name = "route_item", catalog = "adhocrailway", uniqueConstraints = {})
public class RouteItem extends Model implements java.io.Serializable,Comparable<RouteItem> {

	// Fields    

	@Id @GeneratedValue
	private int id;

	private Turnout turnout;

	private Route route;

	private String routedState;
	
	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_TURNOUT = "turnout";
	public static final String PROPERTYNAME_ROUTE = "route";
	public static final String PROPERTYNAME_ROUTED_STATE = "routedState";


	public int compareTo(RouteItem o) {
		if(this == o) return 0;
		if(o == null) return -1;
		return turnout.compareTo(o.getTurnout());
	}
	
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final RouteItem l = (RouteItem) o;
		if (id != l.getId())
			return false;
		if (!turnout.equals(l.getTurnout()))
			return false;
		if (!routedState.equals(l.getRoutedState()))
			return false;
		return true;
	}

	public int hashCode() {
		return turnout.hashCode() + routedState.hashCode();
	}
	
	public String toString() {
		return turnout.toString() + ":"+routedState;
	}
	
	@Transient
	public TurnoutState getRoutedStateEnum() {
		if (routedState.toUpperCase().equals("STRAIGHT")) {
			return TurnoutState.STRAIGHT;
		} else if (routedState.toUpperCase().equals("LEFT")) {
			return TurnoutState.LEFT;
		} else if (routedState.toUpperCase().equals("RIGHT")) {
			return TurnoutState.RIGHT;
		}
		return TurnoutState.UNDEF;
	}
	
	public void setRoutedStateEnum(TurnoutState state) {
		switch (state) {
		case STRAIGHT:
			setRoutedState("STRAIGHT");
			break;
		case LEFT:
			setRoutedState("LEFT");
			break;
		case RIGHT:
			setRoutedState("RIGHT");
			break;
		default:
			setRoutedState("UNDEF");
		}
	}
	
	// Constructors

	/** default constructor */
	public RouteItem() {
	}

	/** full constructor */
	public RouteItem(int id, Turnout turnout, Route route, String routedState) {
		this.id = id;
		this.turnout = turnout;
		this.route = route;
		this.routedState = routedState;
	}

	// Property accessors

	@Id @GeneratedValue
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = true)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		int old = this.id;
		this.id = id;
		firePropertyChange(PROPERTYNAME_ID, old, id);
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "turnout_id", unique = false, nullable = false, insertable = true, updatable = true)
	public Turnout getTurnout() {
		return this.turnout;
	}

	public void setTurnout(Turnout turnout) {
		Turnout old = this.turnout;
		this.turnout = turnout;
		firePropertyChange(PROPERTYNAME_TURNOUT, old, turnout);
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "route_id", unique = false, nullable = false, insertable = true, updatable = true)
	public Route getRoute() {
		return this.route;
	}

	public void setRoute(Route route) {
		Route old = this.route;
		this.route = route;
		firePropertyChange(PROPERTYNAME_ROUTE, old, route);
	}

	@Column(name = "routed_state", unique = false, nullable = false, insertable = true, updatable = true, length = 9)
	public String getRoutedState() {
		return this.routedState;
	}

	public void setRoutedState(String routedState) {
		String old = this.routedState;
		this.routedState = routedState;
		firePropertyChange(PROPERTYNAME_ROUTED_STATE, old, routedState);
	}

}
