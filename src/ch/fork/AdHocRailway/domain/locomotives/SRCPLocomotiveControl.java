/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/LocomotiveControl.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:01 BST 2006
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

package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.InvalidAddressException;
import ch.fork.AdHocRailway.domain.LookupAddress;
import ch.fork.AdHocRailway.domain.NoSessionException;
import ch.fork.AdHocRailway.domain.locking.LockingException;
import ch.fork.AdHocRailway.domain.locking.SRCPLockControl;
import ch.fork.AdHocRailway.domain.locomotives.SRCPLocomotive.Direction;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;
import de.dermoba.srcp.devices.GLInfoListener;

/**
 * Controls all actions which can be performed on Locomotives.
 * 
 * @author fork
 * 
 */
public class SRCPLocomotiveControl implements GLInfoListener, Constants,
		LocomotiveControlface {
	private static Logger					logger		=
																Logger
																		.getLogger(SRCPLocomotiveControl.class);
	
	private static LocomotiveControlface	instance;
	private LocomotivePersistenceIface		persistence;
	private List<LocomotiveChangeListener>	listeners;
	private Map<Locomotive, SRCPLocomotive>	srcpLocomotives;
	private SRCPLockControl					lockControl	=
																SRCPLockControl
																		.getInstance();
	
	private SRCPSession						session;
	
	private SRCPLocomotiveControl() {
		logger.info("SRCPLocomotiveControl loaded");
		listeners = new ArrayList<LocomotiveChangeListener>();
		srcpLocomotives = new HashMap<Locomotive, SRCPLocomotive>();
	}
	
	public void update() {
		srcpLocomotives.clear();
		for (Locomotive l : persistence.getAllLocomotives()) {
			SRCPLocomotive sLocomotive = new SRCPLocomotive(l);
			srcpLocomotives.put(l, sLocomotive);
			sLocomotive.setSession(session);
		}
	}
	
	/**
	 * Gets an instance of a LocomotiveControl.
	 * 
	 * @return an instance of LocomotiveControl
	 */
	public static LocomotiveControlface getInstance() {
		if (instance == null) {
			instance = new SRCPLocomotiveControl();
		}
		return instance;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#setSession(de.dermoba.srcp.client.SRCPSession)
	 */
	public void setSession(SRCPSession session) {
		this.session = session;
		for (SRCPLocomotive l : srcpLocomotives.values()) {
			l.setSession(session);
		}
		session.getInfoChannel().addGLInfoListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#toggleDirection(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void toggleDirection(Locomotive locomotive)
			throws LocomotiveException {
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		switch (sLocomotive.direction) {
		case FORWARD:
			sLocomotive.setDirection(Direction.REVERSE);
			break;
		case REVERSE:
			sLocomotive.setDirection(Direction.FORWARD);
			break;
		}
	}
	
	public Direction getDirection(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		if (sLocomotive == null)
			return Direction.UNDEF;
		return sLocomotive.getDirection();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#getCurrentSpeed(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public int getCurrentSpeed(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		if (sLocomotive == null)
			return 0;
		return sLocomotive.getCurrentSpeed();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#setSpeed(ch.fork.AdHocRailway.domain.locomotives.Locomotive,
	 *      int, boolean[])
	 */
	public void setSpeed(Locomotive locomotive, int speed, boolean[] functions)
			throws LocomotiveException {
		
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		try {
			if (functions == null) {
				functions = sLocomotive.getFunctions();
			}
			LocomotiveType lt = locomotive.getLocomotiveType();
			int drivingSteps = lt.getDrivingSteps();
			if (speed < 0 || speed > drivingSteps) {
				return;
			}
			GL gl = sLocomotive.getGL();
			switch (sLocomotive.direction) {
			case FORWARD:
				gl.set(SRCPLocomotive.FORWARD_DIRECTION, speed, drivingSteps,
						functions);
				break;
			case REVERSE:
				gl.set(SRCPLocomotive.REVERSE_DIRECTION, speed, drivingSteps,
						functions);
				break;
			case UNDEF:
				gl.set(SRCPLocomotive.FORWARD_DIRECTION, speed, drivingSteps,
						functions);
				sLocomotive.setDirection(Direction.FORWARD);
				break;
			}
			sLocomotive.setCurrentSpeed(speed);
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new LocomotiveLockedException(ERR_LOCKED);
			} else {
				throw new LocomotiveException(ERR_FAILED, x);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#increaseSpeed(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		int newSpeed = sLocomotive.getCurrentSpeed() + 1;
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, sLocomotive.getFunctions());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#decreaseSpeed(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		int newSpeed = sLocomotive.getCurrentSpeed() - 1;
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, sLocomotive.getFunctions());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#increaseSpeedStep(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void increaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		int newSpeed =
				sLocomotive.getCurrentSpeed()
						+ locomotive.getLocomotiveType().getStepping();
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, sLocomotive.getFunctions());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#decreaseSpeedStep(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void decreaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		int newSpeed =
				sLocomotive.getCurrentSpeed()
						- locomotive.getLocomotiveType().getStepping();
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, sLocomotive.getFunctions());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#setFunctions(ch.fork.AdHocRailway.domain.locomotives.Locomotive,
	 *      boolean[])
	 */
	public void setFunctions(Locomotive locomotive, boolean[] functions)
			throws LocomotiveException {
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		setSpeed(locomotive, sLocomotive.getCurrentSpeed(), functions);
	}
	
	public boolean[] getFunctions(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		if (sLocomotive == null)
			return new boolean[0];
		
		return sLocomotive.getFunctions();
	}
	
	public void GLinit(double timestamp, int bus, int address, String protocol,
			String[] params) {
		logger.debug("GLinit( "
				+ bus
				+ " , "
				+ address
				+ " , "
				+ protocol
				+ " , "
				+ Arrays.toString(params)
				+ " )");
		Locomotive locomotive =
				persistence.getLocomotiveByBusAddress(bus, address);
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		if (locomotive != null) {
			try {
				sLocomotive.getGL().get();
			} catch (SRCPException e) {
			}
			informListeners(locomotive);
		}
	}
	
	public void GLset(double timestamp, int bus, int address, String drivemode,
			int v, int vMax, boolean[] functions) {
		
		logger.debug("GLset( "
				+ bus
				+ " , "
				+ address
				+ " , "
				+ drivemode
				+ " , "
				+ v
				+ " , "
				+ vMax
				+ " , "
				+ Arrays.toString(functions)
				+ " )");
		Locomotive locomotive =
				persistence.getLocomotiveByBusAddress(bus, address);
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		if (locomotive != null) {
			if (drivemode.equals(SRCPLocomotive.FORWARD_DIRECTION)) {
				sLocomotive.setDirection(Direction.FORWARD);
			} else if (drivemode.equals(SRCPLocomotive.REVERSE_DIRECTION)) {
				sLocomotive.setDirection(Direction.REVERSE);
			}
			sLocomotive.setCurrentSpeed(v);
			sLocomotive.setFunctions(functions);
			informListeners(locomotive);
		}
	}
	
	public void GLterm(double timestamp, int bus, int address) {
		logger.debug("GLterm( " + bus + " , " + address + " )");
		Locomotive locomotive =
				persistence.getLocomotiveByBusAddress(bus, address);
		checkLocomotive(locomotive);
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		if (locomotive != null) {
			sLocomotive.setGL(null);
			sLocomotive.setInitialized(false);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#addLocomotiveChangeListener(ch.fork.AdHocRailway.domain.locomotives.Locomotive,
	 *      ch.fork.AdHocRailway.domain.locomotives.LocomotiveChangeListener)
	 */
	public void addLocomotiveChangeListener(Locomotive loco,
			LocomotiveChangeListener l) {
		listeners.add(l);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#removeAllLocomotiveChangeListener()
	 */
	public void removeAllLocomotiveChangeListener() {
		listeners.clear();
	}
	
	private void informListeners(Locomotive changedLocomotive) {
		for (LocomotiveChangeListener l : listeners)
			l.locomotiveChanged(changedLocomotive);
		
	}
	
	private void checkLocomotive(Locomotive locomotive)
			throws LocomotiveException {
		if (locomotive == null)
			return;
		if (locomotive.getAddress() == 0) {
			throw new LocomotiveException("Locomotive "
					+ locomotive.getName()
					+ " has an invalid bus or address",
					new InvalidAddressException());
		}
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		if (sLocomotive == null) {
			srcpLocomotives.put(locomotive, new SRCPLocomotive(locomotive));
			sLocomotive = srcpLocomotives.get(locomotive);
		}
		if (sLocomotive.getSession() == null && session == null) {
			throw new LocomotiveException(Constants.ERR_NOT_CONNECTED,
					new NoSessionException());
		}
		if (sLocomotive.getSession() == null && session != null) {
			sLocomotive.setSession(session);
		}
		
		initLocomotive(locomotive);
	}
	
	private void initLocomotive(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = srcpLocomotives.get(locomotive);
		if (!sLocomotive.isInitialized()) {
			try {
				GL gl = new GL(session);
				LocomotiveType lt = locomotive.getLocomotiveType();
				String[] params = new String[3];
				params[0] = Integer.toString(LocomotiveType.PROTOCOL_VERSION);
				params[1] = Integer.toString(lt.getDrivingSteps());
				params[2] = Integer.toString(lt.getFunctionCount());
				gl.init(locomotive.getBus(), locomotive.getAddress(),
						LocomotiveType.PROTOCOL, params);
				sLocomotive.setInitialized(true);
				sLocomotive.setGL(gl);
				gl.get();
				lockControl.registerControlObject("GL", new LookupAddress(
						locomotive.getBus(), locomotive.getAddress()),
						locomotive);
			} catch (SRCPDeviceLockedException x1) {
				throw new LocomotiveLockedException(ERR_LOCKED, x1);
			} catch (SRCPException x) {
				throw new LocomotiveException(ERR_INIT_FAILED, x);
			}
		}
	}
	
	public void setLocomotivePersistence(LocomotivePersistenceIface persistence) {
		this.persistence = persistence;
		
	}
	
	public boolean acquireLock(Locomotive object) throws LockingException {
		checkLocomotive(object);
		boolean locked =
				lockControl.acquireLock("GL", new LookupAddress(
						object.getBus(), object.getAddress()));
		return locked;
	}
	
	public boolean releaseLock(Locomotive object) throws LockingException {
		checkLocomotive(object);
		return lockControl.releaseLock("GL", new LookupAddress(object.getBus(),
				object.getAddress()));
	}
	
	public boolean isLocked(Locomotive object) throws LockingException {
		checkLocomotive(object);
		return lockControl.isLocked("GL", new LookupAddress(object.getBus(),
				object.getAddress()));
	}
	
	public boolean isLockedByMe(Locomotive object) throws LockingException {
		checkLocomotive(object);
		int sessionID =
				lockControl.getLockingSessionID("GL", new LookupAddress(object
						.getBus(), object.getAddress()));
		if (sessionID == session.getCommandChannelID()) {
			return true;
		} else {
			return false;
		}
	}
	
}
