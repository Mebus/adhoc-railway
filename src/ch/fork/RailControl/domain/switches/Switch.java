/*------------------------------------------------------------------------
 * 
 * <Switch.java>  -  <Represents a switch>
 * 
 * begin     : Tue Jan  3 21:24:40 CET 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : bm@fork.ch
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

package ch.fork.RailControl.domain.switches;

import java.awt.Image;
import java.awt.image.ImageObserver;

import ch.fork.RailControl.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;

public abstract class Switch {

    protected int number;
    protected int bus;
    protected Address address;
    protected String desc;
    public enum SwitchState {
		LEFT, STRAIGHT, RIGHT, UNDEF
	};
	protected SwitchState switchState = SwitchState.UNDEF;
	protected SwitchState defaultState = SwitchState.STRAIGHT;
	protected boolean initialized = false;
	public enum SwitchOrientation {
		NORTH, SOUTH, WEST, EAST
	};
	protected SwitchOrientation switchOrientation = SwitchOrientation.EAST;

    protected int SWITCH_PORT_ACTIVATE = 1;
    protected int SWITCH_PORT_DEACTIVATE = 0;
    protected int SWITCH_DELAY  = 100;
    protected String ERR_SWITCH_LOCKED  = "Switch locked";
    protected String ERR_TOGGLE_FAILED  = "Toggle of switch failed";
    protected String ERR_INIT_FAILED  = "Init failed";
    protected String ERR_REINIT_FAILED  = "ReInit failed";
    protected String ERR_NO_SESSION  = "Not connected";
	protected SRCPSession session;

	public Switch(int number, String desc, int bus, Address address) {
		this.number = number;
		this.bus = bus;
		this.address = address;
		this.desc = desc;
	}
	
	public void init() throws SwitchException {
		if(session == null) {
			throw new SwitchException(ERR_NO_SESSION);
		}
	}
	protected abstract void reinit() throws SwitchException;
    protected abstract void toggle() throws SwitchException;
    protected abstract void setStraight() throws SwitchException;
    protected abstract void setCurvedLeft() throws SwitchException;
    protected abstract void setCurvedRight() throws SwitchException;
    protected abstract void switchPortChanged(int pAddress, int pChangedPort, int value);
    protected abstract void switchInitialized(int pBus, int pAddress);
    protected abstract void switchTerminated(int pAddress);
    /**
     * Get name.
     *
     * @return name as String.
     */
    public int getNumber()
    {
        return number;
    }
    
    /**
     * Set name.
     *
     * @param name the value to set.
     */
    public void setNumber(int number)
    {
        this.number = number;
    }
    
    /**
     * Get desc.
     *
     * @return desc as String.
     */
    public String getDesc()
    {
        return desc;
    }
    
    /**
     * Set desc.
     *
     * @param desc the value to set.
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }
    
    public String getType() {
        return this.getClass().getSimpleName();
    }

	public int getBus() {
		return bus;
	}

	public void setBus(int bus) {
		this.bus = bus;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) throws SwitchException {
		this.address = address;
		reinit();
	}

	public SRCPSession getSession() {
		return session;
	}

	public void setSession(SRCPSession session) {
		this.session = session;
	}

	public boolean isInitialized() {
		return initialized;
	}
	
	public boolean equals(Switch aSwitch) {
		if(address == aSwitch.getAddress() && bus == aSwitch.getBus()) {
			return true;
		} else {
			return false;
		}
	}

	public SwitchState getDefaultState() {
		return defaultState;
	}

	public void setDefaultState(SwitchState defaultState) {
		this.defaultState = defaultState;
	}

	public SwitchOrientation getSwitchOrientation() {
		return switchOrientation;
	}

	public void setSwitchOrientation(SwitchOrientation switchOrientation) {
		this.switchOrientation = switchOrientation;
	}

	public SwitchState getSwitchState() {
		return switchState;
	}
}
