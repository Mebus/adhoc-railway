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
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.power.PowerSupply;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
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
			.getLogger(PersistenceManager.class);
	private static final String SRCP_SERVER_TCP_LOCAL = "_srcpd._tcp.local.";
	private final ApplicationContext appContext;
	private JmDNS adhocServermDNS;
	private final AdHocRailwayIface mainApp;
	private final Preferences preferences;

	public RailwayDeviceManager(final ApplicationContext appContext) {
		this.appContext = appContext;
		mainApp = appContext.getMainApp();
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
		if (railwayDevive.equals(RailwayDevice.SRCP)) {
			final String host = preferences.getStringValue(SRCP_HOSTNAME);
			final int port = preferences.getIntValue(SRCP_PORT);
			connectToSRCPServer(host, port);
		} else {
			mainApp.connectedToRailwayDevice(true);
		}
	}

	private void connectToSRCPServer(final String host, final int port) {
		try {
			final SRCPSession session = new SRCPSession(host, port, false);
			appContext.setSession(session);
			session.getCommandChannel().addCommandDataListener(this);
			session.getInfoChannel().addInfoDataListener(this);
			setSessionOnControllers(session);
			session.connect();
			mainApp.connectedToRailwayDevice(true);

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

	public void autoConnect() {

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
		LOGGER.info("Info received" + infoData.trim());
	}

	public void disconnect() {

		final String railwayDeviceString = preferences
				.getStringValue(RAILWAY_DEVICE);
		final RailwayDevice railwayDevive = RailwayDevice
				.fromString(railwayDeviceString);
		if (railwayDevive.equals(RailwayDevice.SRCP)) {
			disconnectFromSRCPServer();
		} else {
			mainApp.connectedToRailwayDevice(false);
		}

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

			mainApp.connectedToRailwayDevice(false);
			mainApp.updateCommandHistory("Disconnected from server " + host
					+ " on port " + port);
		} catch (final SRCPException e1) {
			mainApp.handleException(e1);
		} catch (final LocomotiveException e1) {
			mainApp.handleException(e1);
		}
	}
}
