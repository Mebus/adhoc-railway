package ch.fork.AdHocRailway.controllers.impl.srcp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import de.dermoba.srcp.model.locomotives.DoubleMMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.MMDeltaLocomotive;
import de.dermoba.srcp.model.locomotives.MMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;

public class SRCPLocomotiveControlAdapterTest {

	private SRCPLocomotiveControlAdapter adapter;

	@Before
	public void setup() {
		adapter = new SRCPLocomotiveControlAdapter();
	}

	@Test(expected = IllegalArgumentException.class)
	public void createSRCPLocomotiveFromNull() {
		adapter.addOrUpdateLocomotive(null);
	}

	@Test
	public void addDeltaSRCPLocomotive() {
		final Locomotive locomotive = createLocomotive(LocomotiveType.DELTA);
		adapter.addOrUpdateLocomotive(locomotive);
		final SRCPLocomotive srcpLocomotive = adapter
				.getSrcpLocomotive(locomotive);

		assertTrue(srcpLocomotive instanceof MMDeltaLocomotive);
		assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
		assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
	}

	@Test
	public void addDigitalSRCPLocomotive() {
		final Locomotive locomotive = createLocomotive(LocomotiveType.DIGITAL);
		adapter.addOrUpdateLocomotive(locomotive);
		final SRCPLocomotive srcpLocomotive = adapter
				.getSrcpLocomotive(locomotive);

		assertTrue(srcpLocomotive instanceof MMDigitalLocomotive);
		assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
		assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
	}

	@Test
	public void addSimulatedMFXSRCPLocomotive() {
		final Locomotive locomotive = createLocomotive(LocomotiveType.SIMULATED_MFX);
		adapter.addOrUpdateLocomotive(locomotive);
		final DoubleMMDigitalLocomotive srcpLocomotive = (DoubleMMDigitalLocomotive) adapter
				.getSrcpLocomotive(locomotive);

		assertTrue(srcpLocomotive instanceof DoubleMMDigitalLocomotive);
		assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
		assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
		assertEquals(locomotive.getAddress2(), srcpLocomotive.getAddress2());
	}

	@Test
	public void updateExistingDeltaLocomotive() {
		final Locomotive locomotive = createLocomotive(LocomotiveType.DELTA);
		adapter.addOrUpdateLocomotive(locomotive);
		SRCPLocomotive srcpLocomotive = adapter.getSrcpLocomotive(locomotive);

		assertTrue(srcpLocomotive instanceof MMDeltaLocomotive);
		assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
		assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());

		locomotive.setBus(2);
		locomotive.setAddress1(3);
		adapter.addOrUpdateLocomotive(locomotive);
		srcpLocomotive = adapter.getSrcpLocomotive(locomotive);

		assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
		assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
	}

	@Test
	public void updateExistingSimulatedMFXLocomotive() {
		final Locomotive locomotive = createLocomotive(LocomotiveType.SIMULATED_MFX);
		adapter.addOrUpdateLocomotive(locomotive);
		DoubleMMDigitalLocomotive srcpLocomotive = (DoubleMMDigitalLocomotive) adapter
				.getSrcpLocomotive(locomotive);

		assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
		assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
		assertEquals(locomotive.getAddress2(), srcpLocomotive.getAddress2());

		locomotive.setBus(2);
		locomotive.setAddress1(3);
		locomotive.setAddress2(4);
		adapter.addOrUpdateLocomotive(locomotive);
		srcpLocomotive = (DoubleMMDigitalLocomotive) adapter
				.getSrcpLocomotive(locomotive);

		assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
		assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
	}

	private Locomotive createLocomotive(final LocomotiveType type) {
		final Locomotive locomotive = new Locomotive();
		locomotive.setId(1);
		locomotive.setName("testname");
		locomotive.setDesc("description");
		locomotive.setBus(1);
		locomotive.setAddress1(1);
		locomotive.setAddress2(2);
		locomotive.setImage("image.png");
		locomotive.setType(type);
		return locomotive;
	}

}
