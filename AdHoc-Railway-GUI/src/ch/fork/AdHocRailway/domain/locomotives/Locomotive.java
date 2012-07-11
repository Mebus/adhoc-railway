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

package ch.fork.AdHocRailway.domain.locomotives;

import com.jgoodies.binding.beans.Model;

public class Locomotive extends Model implements java.io.Serializable,
		Comparable<Locomotive> {

	private int id;

	private LocomotiveGroup locomotiveGroup;

	private LocomotiveType locomotiveType;

	private String name;

	private String description;

	private String image;

	private int address;

	private int bus;

	private int locomotiveTypeId;

	private int locomotiveGroupId;

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_LOCOMOTIVE_GROUP = "locomotiveGroup";
	public static final String PROPERTYNAME_LOCOMOTIVE_TYPE = "locomotiveType";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_DESCRIPTION = "description";
	public static final String PROPERTYNAME_IMAGE = "image";
	public static final String PROPERTYNAME_ADDRESS = "address";
	public static final String PROPERTYNAME_BUS = "bus";

	@Override
	public int compareTo(Locomotive o) {
		if (this == o)
			return 0;
		if (o == null)
			return -1;
		if (name == null) {
			if (id > o.getId())
				return 1;
			else if (id == o.getId())
				return 0;
			else
				return -1;
		} else {
			return name.compareTo(o.getName());
		}

	}

	@Override
	public String toString() {
		return name;
	}

	public Locomotive() {
	}

	public Locomotive(int id, LocomotiveGroup locomotiveGroup,
			LocomotiveType locomotiveType, String name, int address, int bus) {
		this.id = id;
		this.locomotiveGroup = locomotiveGroup;
		this.locomotiveType = locomotiveType;
		this.name = name;
		this.address = address;
		this.bus = bus;

	}

	public Locomotive(int id, LocomotiveGroup locomotiveGroup,
			LocomotiveType locomotiveType, String name, String description,
			String image, int address, int bus) {
		this.id = id;
		this.locomotiveGroup = locomotiveGroup;
		this.locomotiveType = locomotiveType;
		this.name = name;
		this.description = description;
		this.image = image;
		this.address = address;
		this.bus = bus;

	}

	// Property accessors

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		int old = this.id;
		this.id = id;
		firePropertyChange(PROPERTYNAME_ID, old, id);
	}

	public LocomotiveGroup getLocomotiveGroup() {
		return this.locomotiveGroup;
	}

	public void setLocomotiveGroup(LocomotiveGroup locomotiveGroup) {
		LocomotiveGroup old = this.locomotiveGroup;
		this.locomotiveGroup = locomotiveGroup;
		firePropertyChange(PROPERTYNAME_LOCOMOTIVE_GROUP, old, locomotiveGroup);
	}

	public LocomotiveType getLocomotiveType() {
		return this.locomotiveType;
	}

	public void setLocomotiveType(LocomotiveType locomotiveType) {
		LocomotiveType old = this.locomotiveType;
		this.locomotiveType = locomotiveType;
		firePropertyChange(PROPERTYNAME_LOCOMOTIVE_TYPE, old, locomotiveType);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange(PROPERTYNAME_NAME, old, name);
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		String old = this.description;
		this.description = description;
		firePropertyChange(PROPERTYNAME_DESCRIPTION, old, description);
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		String old = this.image;
		this.image = image;
		firePropertyChange(PROPERTYNAME_IMAGE, old, image);
	}

	public int getAddress() {
		return this.address;
	}

	public void setAddress(int address) {
		int old = this.address;
		this.address = address;
		firePropertyChange(PROPERTYNAME_ADDRESS, old, address);
	}

	public int getBus() {
		return this.bus;
	}

	public void setBus(int bus) {
		int old = this.bus;
		this.bus = bus;
		firePropertyChange(PROPERTYNAME_BUS, old, bus);
	}

	public int[] getAddresses() {
		return new int[] { address };
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
		result = prime * result + bus;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result
				+ ((locomotiveGroup == null) ? 0 : locomotiveGroup.hashCode());
		result = prime * result
				+ ((locomotiveType == null) ? 0 : locomotiveType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		final Locomotive other = (Locomotive) obj;
		if (address != other.address)
			return false;
		if (bus != other.bus)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (locomotiveGroup == null) {
			if (other.locomotiveGroup != null)
				return false;
		} else if (!locomotiveGroup.equals(other.locomotiveGroup))
			return false;
		if (locomotiveType == null) {
			if (other.locomotiveType != null)
				return false;
		} else if (!locomotiveType.equals(other.locomotiveType))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public int getLocomotiveTypeId() {
		return locomotiveTypeId;
	}

	public void setLocomotiveTypeId(int locomotiveTypeId) {
		this.locomotiveTypeId = locomotiveTypeId;
	}

	public int getLocomotiveGroupId() {
		return locomotiveGroupId;
	}

	public void setLocomotiveGroupId(int locomotiveGroupId) {
		this.locomotiveGroupId = locomotiveGroupId;
	}

}
