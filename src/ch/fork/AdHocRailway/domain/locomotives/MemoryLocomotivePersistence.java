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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.LookupAddress;

import com.jgoodies.binding.list.ArrayListModel;

public class MemoryLocomotivePersistence implements LocomotivePersistenceIface {
	private static MemoryLocomotivePersistence	instance;

	private static Logger						logger	= Logger
																.getLogger(LocomotivePersistenceIface.class);

	private ArrayListModel<LocomotiveGroup>		locomotiveGroupCache;
	private ArrayListModel<Locomotive>			locomotiveCache;
	private Map<LookupAddress, Locomotive>		addressLocomotiveCache;

	private Map<String, LocomotiveType>			locomotiveTypes;

	private MemoryLocomotivePersistence() {
		logger.info("MemoryLocomotivePersistence loded");
		this.locomotiveCache = new ArrayListModel<Locomotive>();
		this.locomotiveGroupCache = new ArrayListModel<LocomotiveGroup>();
		this.addressLocomotiveCache = new HashMap<LookupAddress, Locomotive>();

		locomotiveTypes = new HashMap<String, LocomotiveType>();

		if (getLocomotiveTypeByName("DELTA") == null) {
			LocomotiveType deltaType = new LocomotiveType(0, "DELTA");
			deltaType.setDrivingSteps(14);
			deltaType.setStepping(4);
			deltaType.setFunctionCount(4);
			addLocomotiveType(deltaType);
		}

		if (getLocomotiveTypeByName("DIGITAL") == null) {
			LocomotiveType digitalType = new LocomotiveType(0, "DIGITAL");
			digitalType.setDrivingSteps(28);
			digitalType.setStepping(2);
			digitalType.setFunctionCount(5);
			addLocomotiveType(digitalType);
		}

	}

	public static MemoryLocomotivePersistence getInstance() {
		if (instance == null) {
			instance = new MemoryLocomotivePersistence();
		}
		return instance;
	}

	public void clear() {
		locomotiveCache.clear();
		locomotiveGroupCache.clear();
		addressLocomotiveCache.clear();
		locomotiveTypes.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#preload()
	 */
	public void preload() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotives()
	 */

	public ArrayListModel<Locomotive> getAllLocomotives() {
		//logger.debug("getAllLocomotives()");
		return locomotiveCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveByAddress(int)
	 */
	@SuppressWarnings("unchecked")
	public Locomotive getLocomotiveByBusAddress(int bus, int address) {
		return addressLocomotiveCache
				.get(new LookupAddress(bus, address, 0, 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void addLocomotive(Locomotive locomotive) {
		addressLocomotiveCache.put(new LookupAddress(locomotive.getBus(),
				locomotive.getAddress(), 0, 0), locomotive);
		locomotiveCache.add(locomotive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void deleteLocomotive(Locomotive locomotive) {

		locomotiveCache.remove(locomotive);
		addressLocomotiveCache.values().remove(locomotive);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void updateLocomotive(Locomotive locomotive) {

	}

	public ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups() {
		//logger.debug("getAllLocomotiveGroups()");
		return locomotiveGroupCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		if (!group.getLocomotives().isEmpty()) {
			throw new LocomotivePersistenceException(
					"Cannot delete locomotive group with associated locomotives");
		}
		locomotiveGroupCache.remove(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void updateLocomotiveGroup(LocomotiveGroup group) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotiveTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveType> getAllLocomotiveTypes() {
		return new TreeSet<LocomotiveType>(locomotiveTypes.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveTypeByName(java.lang.String)
	 */
	public LocomotiveType getLocomotiveTypeByName(String typeName) {
		for (LocomotiveType type : locomotiveTypes.values()) {
			if (type.getTypeName().equals(typeName))
				return type;
		}
		return null;
	}

	public void addLocomotiveGroup(LocomotiveGroup group) {
		locomotiveGroupCache.add(group);

	}

	public void addLocomotiveType(LocomotiveType type) {
		locomotiveTypes.put(type.getTypeName(), type);
	}

	public void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException {
		if (!type.getLocomotives().isEmpty()) {
			throw new LocomotivePersistenceException(
					"Cannot delete locomotive type with associated locomotives");
		}
		locomotiveTypes.values().remove(type);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#flush()
	 */
	public void flush() throws LocomotivePersistenceException {
		logger.debug("flush()");
	}
}
