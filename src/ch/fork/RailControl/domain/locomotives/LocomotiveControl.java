package ch.fork.RailControl.domain.locomotives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GLInfoListener;

public class LocomotiveControl implements GLInfoListener {

	private static LocomotiveControl instance;
	private List<LocomotiveChangeListener> listeners;
	private Map<Integer, Locomotive> locomotives;
	private SRCPSession session;

	private LocomotiveControl() {
		listeners = new ArrayList<LocomotiveChangeListener>();
	}

	public static LocomotiveControl getInstance() {
		if (instance == null) {
			instance = new LocomotiveControl();
			return instance;
		} else {
			return instance;
		}
	}
	
	public void registerLocomotives(List<Locomotive> locomotivesToRegister) {
		locomotives = new HashMap<Integer, Locomotive>();
		for(Locomotive l : locomotivesToRegister) {
			locomotives.put(l.getAddress(), l);
		}
	}
	
	public void addLocomotiveChangeListener(LocomotiveChangeListener l) {
		listeners.add(l);
	}
	
	public void toggleDirection(Locomotive locomotive) throws LocomotiveException {
		locomotive.toggleDirection();
	}
	
	public void setSpeed(Locomotive locomotive, int speed) throws LocomotiveException {
		locomotive.setSpeed(speed);
	}
	
	public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
		locomotive.increaseSpeed();
	}
	
	public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
		locomotive.decreaseSpeed();
	}
	
	public void GLset(double timestamp, int bus, int address, String drivemode,
			int v, int vMax, boolean[] functions) {
		/*
		System.out.println("GAset(" + bus + " , " + address + " , " + drivemode
				+ " , " + v + " , " + vMax + " , " + functions + " )");
		*/
		Locomotive locomotive = locomotives.get(address);
		locomotive.locomotiveChanged(drivemode, v, vMax, functions);
		informListeners(locomotive);
	}

	public void GLinit(double timestamp, int bus, int address, String protocol,
			String[] params) {
		/*
		System.out.println("GLinit(" + bus + " , " + address + " , "
				+ protocol + " , " + params + " )");
		*/
		Locomotive locomotive = locomotives.get(address);
		locomotive.locomotiveInitialized(bus, address, protocol, params); 
		informListeners(locomotive);
	}

	public void GLterm(double timestamp, int bus, int address) {
		/*
		System.out.println("GLterm( " + bus + " , " + address + " )");
		*/
		Locomotive locomotive = locomotives.get(address);
		locomotive.locomotiveTerminated(); 
		informListeners(locomotive);
	}
	
	private void informListeners(Locomotive changedLocomotive) {
		for (LocomotiveChangeListener l : listeners) {
			l.locomotiveChanged(changedLocomotive);
		}
	}

	public SRCPSession getSession() {
		return session;
	}

	public void setSession(SRCPSession session) {
		this.session = session;
		session.getInfoChannel().addGLInfoListener(this);
	}

}
