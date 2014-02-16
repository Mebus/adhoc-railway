package ch.fork.AdHocRailway.ui;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RailwayDevice;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.controllers.impl.brain.BrainController;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.power.PowerSupply;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.context.AdHocRailwayIface;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;
import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.model.locking.SRCPLockControl;

public class RailwayDeviceManager implements CommandDataListener,
		InfoDataListener, PreferencesKeys {

	private static final Logger LOGGER = Logger
			.getLogger(RailwayDeviceManager.class);
	private static final String SRCP_SERVER_TCP_LOCAL = "_srcpd._tcp.local.";
	private final ApplicationContext appContext;
	private JmDNS adhocServermDNS;
	private final AdHocRailwayIface mainApp;
	private final Preferences preferences;
	private boolean connected = false;

	public RailwayDeviceManager(final ApplicationContext appContext) {
		this.appContext = appContext;
		mainApp = appContext.getMainApp();
		appContext.getMainBus().register(this);
		preferences = appContext.getPreferences();
	}

	public void loadControlLayer() {
		mainApp.initProceeded("Loading Control Layer (Power)");

		final String railwayDeviceString = preferences
				.getStringValue(RAILWAY_DEVICE);
		final RailwayDevice railwayDevive = RailwayDevice
				.fromString(railwayDeviceString);

		final PowerController powerControl = PowerController
				.createPowerController(railwayDevive);

		powerControl.addOrUpdatePowerSupply(new PowerSupply(1));
		appContext.setPowerController(powerControl);

		mainApp.initProceeded("Loading Control Layer (Locomotives)");
		final LocomotiveController locomotiveControl = LocomotiveController
				.createLocomotiveController(railwayDevive);

		appContext.setLocomotiveControl(locomotiveControl);

		mainApp.initProceeded("Loading Control Layer (Turnouts)");
		final TurnoutController turnoutControl = TurnoutController
				.createTurnoutController(railwayDevive);
		appContext.setTurnoutControl(turnoutControl);

		mainApp.initProceeded("Loading Control Layer (Routes)");
		final RouteController routeControl = RouteController
				.createLocomotiveController(railwayDevive, turnoutControl);
		routeControl.setRoutingDelay(Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY));
		appContext.setRouteControl(routeControl);

		mainApp.initProceeded("Loading Control Layer (Locks)");
		appContext.setLockControl(SRCPLockControl.getInstance());
	}

	public void connect() {

		final String railwayDeviceString = preferences
				.getStringValue(RAILWAY_DEVICE);
		final RailwayDevice railwayDevive = RailwayDevice
				.fromString(railwayDeviceString);
		if (RailwayDevice.SRCP.equals(railwayDevive)) {
			final String host = preferences.getStringValue(SRCP_HOSTNAME);
			final int port = preferences.getIntValue(SRCP_PORT);
			connectToSRCPServer(host, port);
		} else if (RailwayDevice.ADHOC_BRAIN.equals(railwayDevive)) {
			connectToBrain(preferences.getStringValue(ADHOC_BRAIN_PORT));
		} else {
			connectToNullDevice();
		}

		connected = true;
		appContext.getMainBus().post(new ConnectedToRailwayEvent(true));
	}

	public void disconnect() {
	
		final String railwayDeviceString = preferences
				.getStringValue(RAILWAY_DEVICE);
		final RailwayDevice railwayDevive = RailwayDevice
				.fromString(railwayDeviceString);
		if (railwayDevive.equals(RailwayDevice.SRCP)) {
			disconnectFromSRCPServer();
		} else {
			disconnectFromBrain();
		}
		connected = false;
		appContext.getMainBus().post(new ConnectedToRailwayEvent(false));
	
	}

	public void autoConnectToRailwayDeviceIfRequested() {
		if (preferences.getBooleanValue(SRCP_AUTOCONNECT)
				&& !preferences
						.getBooleanValue(PreferencesKeys.AUTO_DISCOVER_AND_CONNECT_SERVERS)) {
			try {
				loadControlLayer();
				connect();
			} catch (final Exception x) {

			}
		} else if (preferences
				.getBooleanValue(PreferencesKeys.AUTO_DISCOVER_AND_CONNECT_SERVERS)) {

		}
	}

	public void autoDiscoverAndConnect() {
	
		try {
			adhocServermDNS = JmDNS.create();
			final JmDNS srcpdmDNS = JmDNS.create();
			srcpdmDNS.addServiceListener(SRCP_SERVER_TCP_LOCAL,
					new javax.jmdns.ServiceListener() {
	
						@Override
						public void serviceResolved(final ServiceEvent event) {
							LOGGER.info("resolved SRCPD on " + event);
	
						}
	
						@Override
						public void serviceRemoved(final ServiceEvent event) {
	
						}
	
						@Override
						public void serviceAdded(final ServiceEvent event) {
							final ServiceInfo info = adhocServermDNS
									.getServiceInfo(event.getType(),
											event.getName(), true);
							LOGGER.info("found SRCPD on " + info);
							connectToSRCPServer(info.getInet4Addresses()[0]
									.getHostAddress(), info.getPort());
						}
					});
		} catch (final IOException e) {
			mainApp.handleException(e);
		}
	}

	private void connectToNullDevice() {

	}

	private void connectToBrain(final String stringValue) {
		final BrainController brainController = BrainController.getInstance();
		brainController.connect(stringValue);
	}

	private void disconnectFromBrain() {
		BrainController.getInstance().disconnect();

	}

	private void connectToSRCPServer(final String host, final int port) {
		try {
			final SRCPSession session = new SRCPSession(host, port, false);
			appContext.setSession(session);
			session.getCommandChannel().addCommandDataListener(this);
			session.getInfoChannel().addInfoDataListener(this);
			setSessionOnControllers(session);
			session.connect();

			mainApp.updateCommandHistory("Connected to server " + host
					+ " on port " + port);
		} catch (final SRCPException e) {
			preferences
					.setBooleanValue(PreferencesKeys.SRCP_AUTOCONNECT, false);
			try {
				preferences.save();
			} catch (final IOException e2) {
				mainApp.handleException("Server not running", e2);
			}
			mainApp.handleException("SRCP server not running", e);
		}

		final SRCPTurnoutControlAdapter srcpTurnoutControlAdapter = (SRCPTurnoutControlAdapter) appContext
				.getTurnoutControl();
		srcpTurnoutControlAdapter.registerTurnouts(appContext
				.getTurnoutManager().getAllTurnouts());

	}

	private void disconnectFromSRCPServer() {
		try {
			final String host = preferences.getStringValue(SRCP_HOSTNAME);
			final int port = preferences.getIntValue(SRCP_PORT);

			appContext.getLocomotiveControl().emergencyStopActiveLocos();
			SRCPSession session = appContext.getSession();
			session.disconnect();
			session = null;

			setSessionOnControllers(session);

			mainApp.updateCommandHistory("Disconnected from server " + host
					+ " on port " + port);
		} catch (final SRCPException e1) {
			mainApp.handleException(e1);
		}
	}

	private void setSessionOnControllers(final SRCPSession session) {
		((SRCPPowerControlAdapter) appContext.getPowerControl())
				.setSession(session);
		((SRCPTurnoutControlAdapter) appContext.getTurnoutControl())
				.setSession(session);
		((SRCPLocomotiveControlAdapter) appContext.getLocomotiveControl())
				.setSession(session);
		((SRCPRouteControlAdapter) appContext.getRouteControl())
				.setSession(session);
		appContext.getLockControl().setSession(session);
	}

	@Override
	public void commandDataSent(final String commandData) {
		if (preferences.getBooleanValue(LOGGING)) {
			mainApp.updateCommandHistory("Command sent: " + commandData);
		}
		LOGGER.info("Command sent: " + commandData.trim());
	}

	@Override
	public void commandDataReceived(final String response) {
		if (preferences.getBooleanValue(LOGGING)) {
			mainApp.updateCommandHistory("Command received: " + response);
		}
		LOGGER.info("Command received: " + response.trim());
	}

	@Override
	public void infoDataSent(final String infoData) {
		if (preferences.getBooleanValue(LOGGING)) {
			mainApp.updateCommandHistory("Info sent: " + infoData);
		}
		LOGGER.info("Info sent: " + infoData.trim());
	}

	@Override
	public void infoDataReceived(final String infoData) {
		if (preferences.getBooleanValue(LOGGING)) {
			mainApp.updateCommandHistory("Info received: " + infoData);
		}
		LOGGER.info("Info received " + infoData.trim());
	}

	public boolean isBrainAvailable() {
		final BrainController brainController = BrainController.getInstance();
		try {
			brainController.getAvailableSerialPortsAsString();
			return true;
		} catch (final Exception x) {
			return false;
		}
	}

	public boolean isConnected() {
		return connected;
	}

}
