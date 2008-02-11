/*------------------------------------------------------------------------
 * 
 * <./domain/locking/LockControl.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:45 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
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

package ch.fork.AdHocRailway.domain.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.locking.exception.LockingException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.LOCK;
import de.dermoba.srcp.devices.LOCKInfoListener;

public class SRCPLockControl extends Control implements LOCKInfoListener, Constants, LockControlIface {

	private static SRCPLockControl instance = null;

	private Map<Integer, ControlObject> addressToControlObject;

	private List<LockChangeListener> listeners;

	private SRCPLockControl() {
		addressToControlObject = new HashMap<Integer, ControlObject>();
		listeners = new ArrayList<LockChangeListener>();
	}

	public static SRCPLockControl getInstance() {
		if (instance == null) {
			instance = new SRCPLockControl();
		}
		return instance;
	}

	public void registerControlObject(ControlObject object) {
		int[] addresses = object.getAddresses();
		for (int address : addresses) {
			addressToControlObject.put(address, object);
		}
	}

	public void registerControlObjects(Collection<ControlObject> objects) {
		for (ControlObject anObject : objects) {
			registerControlObject(anObject);
		}
	}

	public void unregisterControlObject(ControlObject object) {
		int[] addresses = object.getAddresses();
		for (int address : addresses) {
			addressToControlObject.remove(address);
		}
	}

	public void unregisterAllControlObjects() {
		addressToControlObject.clear();
	}

	public void setSession(SRCPSession session) {
		this.session = session;
		session.getInfoChannel().addLOCKInfoListener(this);
	}

	public boolean acquireLock(ControlObject object) throws LockingException {

		if (object == null) return false;
		// TODO checkControlObject(object);
		// TODO init(object);
		for (int address : object.getAddresses()) {
			LOCK lock = object.getLock();
			try {
				lock.set(object.getDeviceGroup(), address, Preferences
						.getInstance().getIntValue(
								PreferencesKeys.LOCK_DURATION));
			} catch (SRCPDeviceLockedException e) {
				throw new LockingException(ERR_LOCKED, e);
			} catch (SRCPException e) {
				throw new LockingException(ERR_FAILED, e);
			}
		}
		return true;
	}

	public boolean releaseLock(ControlObject object) throws LockingException {

		if (object == null) return false;
		// TODO checkControlObject(object);
		// TODO init(object);
		for (int address : object.getAddresses()) {
			LOCK lock = object.getLock();
			try {
				lock.term(object.getDeviceGroup(), address);
			} catch (SRCPDeviceLockedException e) {
				throw new LockingException(ERR_LOCKED, e);
			} catch (SRCPException e) {
				throw new LockingException(ERR_FAILED, e);
			}
		}
		return true;
	}

	public void releaseAllLocks() throws LockingException {
		if (session != null) {
			for (ControlObject co : addressToControlObject.values()) {
				if (co.getLockedBySession() == session.getCommandChannelID()) {
					releaseLock(co);
				}
			}
		}
	}

	public void LOCKset(double timestamp, int bus, int address,
			String deviceGroup, int duration, int sessionID) {
		Address addr = new Address(bus, address);
		ControlObject object = addressToControlObject.get(addr);
		if (object != null) {
			object.lockSet(addr, duration, sessionID);
			informListeners(object);
		}
	}

	public void LOCKterm(double timestamp, int bus, int address,
			String deviceGroup) {
		Address addr = new Address(bus, address);
		ControlObject object = addressToControlObject.get(addr);
		if (object != null) {
			object.lockTerm(addr);
			informListeners(object);
		}
	}

	private void informListeners(ControlObject object) {
		for (LockChangeListener l : listeners) {
			l.lockChanged(object);
		}
	}

	public void addLockChangeListener(LockChangeListener l) {
		listeners.add(l);
	}

	public void removeAllLockChangeListener() {
		listeners.clear();
	}

	public ControlObject getControlObject(Address address) {
		return null;
	}

	public int getSessionID() {
		if (session != null)
			return session.getCommandChannelID();
		else
			return -1;
	}
}
