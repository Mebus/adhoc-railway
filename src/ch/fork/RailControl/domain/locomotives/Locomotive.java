/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <Locomotive.java>  -  <>
 * 
 * begin     : Apr 8, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
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

package ch.fork.RailControl.domain.locomotives;

import ch.fork.RailControl.domain.Constants;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveLockedException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;

public abstract class Locomotive implements Constants {
	private String name;
	private String desc;

	private int address;
	private int bus;

	public enum Direction {
		FORWARD, REVERSE, UNDEF
	};

	private Direction direction = Direction.UNDEF;

	private final int PROTOCOL_VERSION = 2;

	private final String PROTOCOL = "M";

	private final String FORWARD_DIRECTION = "1";

	private final String REVERSE_DIRECTION = "0";

	private int drivingSteps;

	private int currentSpeed;

	private SRCPSession session;

	private GL gl;
	
	private boolean[] functions;

	private String[] params;

	private boolean initialized = false;
	
	public Locomotive(String name, int bus, int address,
			int drivingSteps, String desc, int functionCount) {
		this(null, name, bus, address, drivingSteps, desc, functionCount);

	}
	
	public Locomotive(SRCPSession session, String name, int bus, int address,
			int drivingSteps, String desc, int functionCount) {
		this.session = session;
		this.name = name;
		this.bus = bus;
		this.address = address;
		this.drivingSteps = drivingSteps;
		this.desc = desc;
		params = new String[3];
		params[0] = Integer.toString(PROTOCOL_VERSION);
		params[1] = Integer.toString(drivingSteps);
		params[2] = Integer.toString(functionCount);
	}
	

	protected abstract void increaseSpeedStep() throws LocomotiveException;
	protected abstract void decreaseSpeedStep() throws LocomotiveException;

	public void init() throws LocomotiveException {
		try {
			if (session == null) {
				throw new LocomotiveException(ERR_NO_SESSION);
			}
			gl = new GL(session);
			gl.init(bus, address, PROTOCOL, params);
			initialized = true;
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new LocomotiveLockedException(ERR_LOCKED);
			} else {
				throw new LocomotiveException(ERR_INIT_FAILED, x);
			}
		}
	}
	
	protected void reinit() throws LocomotiveException {
		try {
			if(gl != null) {
				gl.term();
			}
		} catch (SRCPException e) {
			throw new LocomotiveException(ERR_REINIT_FAILED, e);
		}
		if(session != null) {
			init();
		}
	}
	
	protected void term() throws LocomotiveException {
		try {
			if(gl != null) {
				gl.term();
			}
		} catch (SRCPException e) {
			throw new LocomotiveException(ERR_TERM_FAILED, e);
		}
	}

	protected void setSpeed(int speed) throws LocomotiveException {
		try {
			if(speed < 0 || speed > drivingSteps) {
				return;
			}
			switch (direction) {
			case FORWARD:
				gl.set(FORWARD_DIRECTION, speed, drivingSteps, functions);
				break;
			case REVERSE:
				gl.set(REVERSE_DIRECTION, speed, drivingSteps, functions);
				break;
			case UNDEF:
				gl.set(FORWARD_DIRECTION, speed, drivingSteps, functions);
				direction = Direction.FORWARD;
				break;
			}
			currentSpeed = speed;
			// gl.get();
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new LocomotiveLockedException(ERR_LOCKED);
			} else {
				throw new LocomotiveException(ERR_FAILED, x);
			}
		}
	}

	protected void increaseSpeed() throws LocomotiveException {
		int newSpeed = currentSpeed + 1;
		if (newSpeed <= drivingSteps) {
			setSpeed(newSpeed);
			currentSpeed++;
		}
	}

	protected void decreaseSpeed() throws LocomotiveException {
		int newSpeed = currentSpeed - 1;
		if (newSpeed >= 0) {
			setSpeed(newSpeed);
			currentSpeed--;
		}
	}

	protected void toggleDirection() {
		switch (this.direction) {
		case FORWARD:
			direction = Direction.REVERSE;
			break;
		case REVERSE:
			direction = Direction.FORWARD;
			break;
		}
	}
	
	protected void setFunctions(boolean[] functions) throws LocomotiveException {
		this.functions = functions;
		setSpeed(currentSpeed);
	}

	protected void locomotiveChanged(String pDrivemode, int v, int vMax,
			boolean[] functions) {
		if (pDrivemode.equals(FORWARD_DIRECTION)) {
			direction = Direction.FORWARD;
		} else if (pDrivemode.equals(REVERSE_DIRECTION)) {
			direction = Direction.REVERSE;
		}
		currentSpeed = v;
		this.functions = functions;
	}

	protected void locomotiveInitialized(int pBus, int pAddress,
			String protocol, String[] params) {
		gl = new GL(session);
		this.address = pAddress;
		this.bus = pBus;
		gl.setBus(bus);
		gl.setAddress(address);
		initialized = true;
	}

	protected void locomotiveTerminated() {
		gl = null;
		initialized = false;
	}

	public boolean isInitialized() {
		return initialized;
	}

	protected void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public SRCPSession getSession() {
		return session;
	}

	public void setSession(SRCPSession session) {
		this.session = session;
	}

	public int getDrivingSteps() {
		return drivingSteps;
	}

	public String getName() {
		return name;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	public String toString() {
		return name;
	}

	public int getAddress() {
		return address;
	}

	public boolean equals(Locomotive l) {
		if (address == l.getAddress() && bus == l.getBus()) {
			return true;
		} else {
			return false;
		}
	}

	public int getBus() {
		return bus;
	}

	public Direction getDirection() {
		return direction;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return this.getClass().getSimpleName();
	}

	public void setAddress(int address) throws LocomotiveException {
		this.address = address;
		reinit();
	}

	public void setBus(int bus) {
		this.bus = bus;
	}

}