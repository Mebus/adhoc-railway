/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutType.java 199 2012-01-14 23:46:24Z fork_ch $
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

package ch.fork.AdHocRailway.services.turnouts;

// Generated 08-Aug-2007 18:10:44 by Hibernate Tools 3.2.0.beta8

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import com.jgoodies.binding.beans.Model;

import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

/**
 * TurnoutType generated by hbm2java
 */
// @Entity
@Table(name = "turnout_type", uniqueConstraints = {})
public class HibernateTurnoutType extends Model implements
		java.io.Serializable, Comparable<HibernateTurnoutType> {

	// Fields-
	@Id
	@GeneratedValue
	private int id;

	private String typeName;

	@Sort(type = SortType.NATURAL)
	private SortedSet<HibernateTurnout> turnouts = new TreeSet<HibernateTurnout>();

	private static final String PROPERTYNAME_ID = "id";
	private static final String PROPERTYNAME_TYPENAME = "typeName";
	private static final String PROPERTYNAME_TURNOUTS = "turnouts";

	@Override
	public int compareTo(HibernateTurnoutType o) {
		return typeName.compareTo(o.getTypeName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// result = prime * result + id;
		result = prime * result
				+ ((typeName == null) ? 0 : typeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final HibernateTurnoutType other = (HibernateTurnoutType) obj;
		// if (id != other.id)
		// return false;
		if (typeName == null) {
			if (other.typeName != null) {
				return false;
			}
		} else if (!typeName.equals(other.typeName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return typeName;
	}

	@Transient
	public SRCPTurnoutTypes getTurnoutTypeEnum() {
		if (typeName.toUpperCase().equals("DEFAULT")) {
			return SRCPTurnoutTypes.DEFAULT;
		} else if (typeName.toUpperCase().equals("DOUBLECROSS")) {
			return SRCPTurnoutTypes.DOUBLECROSS;
		} else if (typeName.toUpperCase().equals("THREEWAY")) {
			return SRCPTurnoutTypes.THREEWAY;
		} else if (typeName.toUpperCase().equals("CUTTER")) {
			return SRCPTurnoutTypes.CUTTER;
		} else {
			return SRCPTurnoutTypes.UNKNOWN;
		}

	}

	// Constructors

	/** default constructor */
	public HibernateTurnoutType() {
	}

	/** minimal constructor */
	public HibernateTurnoutType(int id, String typeName) {
		this.id = id;
		this.typeName = typeName;
	}

	/** full constructor */
	public HibernateTurnoutType(int id, String typeName,
			SortedSet<HibernateTurnout> turnouts) {
		this.id = id;
		this.typeName = typeName;
		this.turnouts = turnouts;
	}

	// Property accessors
	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = true)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		int old = this.id;
		this.id = id;
		// firePropertyChange(PROPERTYNAME_ID, old, id);
	}

	@Column(name = "type_name", unique = false, nullable = false, insertable = true, updatable = true)
	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(String typeName) {
		String old = this.typeName;
		this.typeName = typeName;
		firePropertyChange(PROPERTYNAME_TYPENAME, old, typeName);
	}

	@Sort(type = SortType.NATURAL)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "turnoutType")
	public SortedSet<HibernateTurnout> getTurnouts() {
		return this.turnouts;
	}

	@Sort(type = SortType.NATURAL)
	public void setTurnouts(SortedSet<HibernateTurnout> turnouts) {
		SortedSet<HibernateTurnout> old = this.turnouts;
		this.turnouts = turnouts;
		// firePropertyChange(PROPERTYNAME_TURNOUTS, old, turnouts);
	}

}
