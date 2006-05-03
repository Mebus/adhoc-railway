package ch.fork.RailControl.domain.locomotives;

import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;

public class DigitalLocomotive extends Locomotive {

	private static final int DRIVING_STEPS = 28;
	private static final int STEPPING = 4;

	public DigitalLocomotive(SRCPSession session, String name, int bus,
			int address, String desc) {
		super(session, name, bus, address, DRIVING_STEPS, desc);
	}
	
	protected void increaseSpeedStep() throws LocomotiveException {
		super.setSpeed(getCurrentSpeed() + STEPPING);
	}
	
	protected void decreaseSpeedStep() throws LocomotiveException {
		super.setSpeed(getCurrentSpeed() - STEPPING);
	}
}
