package ch.fork.AdHocRailway.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.manager.impl.locomotives.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.manager.impl.turnouts.RouteManagerImpl;
import ch.fork.AdHocRailway.manager.impl.turnouts.TurnoutManagerImpl;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.manager.turnouts.RouteManager;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.impl.socketio.ServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.locomotives.SIOLocomotiveService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIORouteService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIOTurnoutService;
import ch.fork.AdHocRailway.services.impl.xml.XMLLocomotiveService;
import ch.fork.AdHocRailway.services.impl.xml.XMLRouteService;
import ch.fork.AdHocRailway.services.impl.xml.XMLServiceHelper;
import ch.fork.AdHocRailway.services.impl.xml.XMLTurnoutService;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;

public class PersistenceManager {

	private static final Logger LOGGER = Logger
			.getLogger(PersistenceManager.class);
	private static final String _ADHOC_SERVER_TCP_LOCAL = "_adhoc-server._tcp.local.";

	private final ApplicationContext appContext;

	public PersistenceManager(final ApplicationContext ctx) {
		this.appContext = ctx;

	}

	public void loadPersistenceLayer() {

		final boolean useAdHocServer = appContext.getPreferences()
				.getBooleanValue(PreferencesKeys.USE_ADHOC_SERVER);

		appContext.getMainApp().initProceeded(
				"Loading Persistence Layer (Locomotives)");
		LocomotiveManager locomotiveManager = appContext.getLocomotiveManager();
		if (locomotiveManager == null) {
			locomotiveManager = new LocomotiveManagerImpl();
			appContext.setLocomotiveManager(locomotiveManager);
		}

		if (useAdHocServer) {
			locomotiveManager.setLocomotiveService(new SIOLocomotiveService());
		} else {
			locomotiveManager.setLocomotiveService(new XMLLocomotiveService());
		}

		locomotiveManager.initialize(appContext.getMainBus());

		appContext.getMainApp().initProceeded(
				"Loading Persistence Layer (Turnouts)");

		TurnoutManager turnoutManager = appContext.getTurnoutManager();
		if (turnoutManager == null) {
			turnoutManager = new TurnoutManagerImpl();
		}
		appContext.setTurnoutManager(turnoutManager);
		if (useAdHocServer) {
			turnoutManager.setTurnoutService(new SIOTurnoutService());
		} else {
			turnoutManager.setTurnoutService(new XMLTurnoutService());
		}
		turnoutManager.initialize(appContext.getMainBus());

		appContext.getMainApp().initProceeded(
				"Loading Persistence Layer (Routes)");
		RouteManager routeManager = appContext.getRouteManager();

		if (routeManager == null) {
			routeManager = new RouteManagerImpl(turnoutManager);
		}
		appContext.setRouteManager(routeManager);
		if (useAdHocServer) {
			routeManager.setRouteService(new SIORouteService());
		} else {
			routeManager.setRouteService(new XMLRouteService());
		}
		routeManager.initialize(appContext.getMainBus());

	}

	public void loadLastFileOrLoadDataFromAdHocServerIfRequested()
			throws IOException {
		final Preferences preferences = appContext.getPreferences();

		final boolean useAdHocServer = preferences
				.getBooleanValue(PreferencesKeys.USE_ADHOC_SERVER);
		if (!useAdHocServer
				&& preferences.getBooleanValue(PreferencesKeys.OPEN_LAST_FILE)) {
			final String lastFile = preferences
					.getStringValue(PreferencesKeys.LAST_OPENED_FILE);
			if (lastFile != null
					&& !lastFile.equals("")
					&& !preferences
							.getBooleanValue(PreferencesKeys.USE_ADHOC_SERVER)) {

				loadFile(new File(
						preferences
								.getStringValue(PreferencesKeys.LAST_OPENED_FILE)));
			}
		} else if (useAdHocServer
				&& !appContext.getPreferences().getBooleanValue(
						PreferencesKeys.AUTO_DISCOVER_AND_CONNECT_SERVERS)) {

			final String url = getAdHocServerURL();
			connectToAdHocServer(url);
		}
	}

	public void switchToFileMode() throws FileNotFoundException, IOException {
		final Preferences preferences = appContext.getPreferences();
		preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, false);
		preferences.save();
	}

	public void switchToServerMode() throws FileNotFoundException, IOException {
		final Preferences preferences = appContext.getPreferences();
		preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, true);
		preferences.save();
	}

	void loadFile(final File file) throws IOException {
		disconnectFromCurrentPersistence();

		switchToFileMode();

		loadPersistenceLayer();

		appContext.getLocomotiveManager().clear();
		appContext.getTurnoutManager().clear();
		appContext.getRouteManager().clear();

		new XMLServiceHelper()
				.loadFile((XMLLocomotiveService) appContext
						.getLocomotiveManager().getService(),
						(XMLTurnoutService) appContext.getTurnoutManager()
								.getService(), (XMLRouteService) appContext
								.getRouteManager().getService(), file);
	}

	public String getAdHocServerURL() {
		final Preferences preferences = appContext.getPreferences();
		final StringBuilder b = new StringBuilder();
		b.append("http://");

		b.append(preferences
				.getStringValue(PreferencesKeys.ADHOC_SERVER_HOSTNAME));
		b.append(":");
		b.append(preferences.getStringValue(PreferencesKeys.ADHOC_SERVER_PORT));
		final String url = b.toString();
		return url;
	}

	public void connectToAdHocServer(final String url) {
		SIOService.getInstance().connect(url, new ServiceListener() {

			@Override
			public void disconnected() {
				appContext.getMainApp().updateCommandHistory(
						"Successfully connected to AdHoc-Server");
			}

			@Override
			public void connectionError(final Exception ex) {
				appContext.getMainApp().updateCommandHistory(
						"Connection error: " + ex.getMessage());
				appContext.getPreferences().setBooleanValue(
						PreferencesKeys.USE_ADHOC_SERVER, false);
				try {
					appContext.getPreferences().save();
				} catch (final IOException e) {
					appContext.getMainApp().handleException(e);
				}
				appContext.getMainApp().handleException(ex);
			}

			@Override
			public void connected() {
				appContext.getMainApp().setTitle(
						AdHocRailway.TITLE + " [" + url + "]");

				appContext.getMainApp().updateCommandHistory(
						"Successfully connected to AdHoc-Server: " + url);

			}
		});
	}

	public void disconnectFromCurrentPersistence() {
		appContext.getTurnoutManager().disconnect();
		appContext.getRouteManager().disconnect();
		appContext.getLocomotiveManager().disconnect();
	}

	public void autoConnect() {
		final JmDNS adhocServermDNS;
		try {
			adhocServermDNS = JmDNS.create();

			adhocServermDNS.addServiceListener(_ADHOC_SERVER_TCP_LOCAL,
					new javax.jmdns.ServiceListener() {

						@Override
						public void serviceResolved(final ServiceEvent event) {
							LOGGER.info("resolved AdHoc-Server on " + event);

						}

						@Override
						public void serviceRemoved(final ServiceEvent event) {

						}

						@Override
						public void serviceAdded(final ServiceEvent event) {
							final ServiceInfo info = adhocServermDNS
									.getServiceInfo(event.getType(),
											event.getName(), true);
							LOGGER.info("found AdHoc-Server on " + info);

							final String url = "http://"
									+ info.getInet4Addresses()[0]
											.getHostAddress() + ":"
									+ info.getPort();

							connectToAdHocServer(url);
						}
					});
		} catch (final IOException e) {

			appContext.getMainApp().handleException(e);
		}
	}

}
