/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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

package ch.fork.AdHocRailway.ui;

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIconFromIconSet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.persistence.PersistenceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.domain.locomotives.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.routes.RouteManagerImpl;
import ch.fork.AdHocRailway.domain.routes.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerImpl;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.impl.socketio.ServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.locomotives.SIOLocomotiveService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIORouteService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIOTurnoutService;
import ch.fork.AdHocRailway.services.impl.xml.XMLLocomotiveService;
import ch.fork.AdHocRailway.services.impl.xml.XMLRouteService;
import ch.fork.AdHocRailway.services.impl.xml.XMLTurnoutService;
import ch.fork.AdHocRailway.technical.configuration.ConfigurationException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.AdHocRailwayIface;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveControlPanel;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfigurationDialog;
import ch.fork.AdHocRailway.ui.power.PowerControlPanel;
import ch.fork.AdHocRailway.ui.routes.configuration.RoutesConfigurationDialog;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfigurationDialog;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import de.dermoba.srcp.model.locking.SRCPLockingException;
import de.dermoba.srcp.model.power.SRCPPowerControl;
import de.dermoba.srcp.model.power.SRCPPowerState;
import de.dermoba.srcp.model.power.SRCPPowerSupplyException;

public class AdHocRailway extends JFrame implements AdHocRailwayIface,
		CommandDataListener, InfoDataListener, PreferencesKeys,
		EditingModeListener {

	private static final String _ADHOC_SERVER_TCP_LOCAL = "_adhoc-server._tcp.local.";

	private static final long serialVersionUID = -6033036063027343513L;

	private static Logger LOGGER = Logger.getLogger(AdHocRailway.class);

	private static final String TITLE = "AdHoc-Railway";

	private static final String SRCP_SERVER_TCP_LOCAL = "_srcpd._tcp.local.";

	private TrackControlPanel trackControlPanel;

	private LocomotiveControlPanel locomotiveControlPanel;

	private JPanel statusBarPanel;

	private JLabel hostnameLabel;

	private JButton connectToolBarButton;

	private JButton disconnectToolBarButton;

	private JComboBox<String> commandHistory;

	private DefaultComboBoxModel<String> commandHistoryModel;

	private JMenuItem daemonConnectItem;

	private JMenuItem daemonDisconnectItem;

	private JMenuItem daemonPowerOnItem;

	private JMenuItem daemonPowerOffItem;

	private JButton toggleFullscreenButton;

	private JMenuBar menuBar;

	boolean fullscreen = false;

	private SplashWindow splash;

	private JPanel mainPanel;
	private JPanel toolbarPanel;
	private File actualFile;
	private JProgressBar progressBar;
	private boolean fileMode;
	private PowerControlPanel powerControlPanel;

	private JMenuItem saveItem;

	private JMenuItem saveAsItem;

	private JMenuItem switchesItem;

	private JMenuItem routesItem;

	private JMenuItem locomotivesItem;

	private JCheckBoxMenuItem enableEditing;

	private final List<EditingModeListener> editingModeListeners = new ArrayList<EditingModeListener>();

	private JButton turnoutsToolBarButton;

	private JButton routesToolBarButton;

	private JButton locomotivesToolBarButton;

	private ApplicationContext appContext;

	private Preferences preferences;

	public AdHocRailway() {
		this(null);
	}

	public AdHocRailway(final String file) {
		super(TITLE);
		try {

			appContext = new ApplicationContext();
			appContext.setMainApp(this);
			appContext.setMainFrame(this);
			setUpLogging();

			LOGGER.info("****************************************");
			LOGGER.info("AdHoc-Railway starting up!!!");
			LOGGER.info("****************************************");

			PlasticLookAndFeel
					.setTabStyle(PlasticLookAndFeel.TAB_STYLE_DEFAULT_VALUE);
			PlasticLookAndFeel.setHighContrastFocusColorsEnabled(false);

			UIManager.setLookAndFeel(new PlasticXPLookAndFeel());

			splash = new SplashWindow(createImageIconFromIconSet("splash.png"),
					this, 500, 12);
			setIconImage(createImageIconFromIconSet("RailControl.png")
					.getImage());

			initProceeded("Loading Persistence Layer (Preferences)");

			preferences = Preferences.getInstance();
			appContext.setPreferences(preferences);

			loadControlLayer();

			loadPersistenceLayer();

			initProceeded("Creating GUI ...");

			initGUI();
			disableEnableMenuItems();
			LOGGER.info("Finished Creating GUI");
			splash.setVisible(false);

			if (preferences.getBooleanValue(OPEN_LAST_FILE)) {
				final String lastFile = preferences
						.getStringValue(LAST_OPENED_FILE);
				if (lastFile != null && !lastFile.equals("") && fileMode) {

					new OpenFileAction().openFile(new File(preferences
							.getStringValue(LAST_OPENED_FILE)));
				}
			}
			updateGUI();
			autoConnect();

			setSize(1200, 1024);
			pack();

			initProceeded("AdHoc-Railway started");
			updateCommandHistory("AdHoc-Railway started");
			setVisible(true);
		} catch (final UnsupportedLookAndFeelException e) {
			handleException(e);
		}
	}

	public void saveActualFile() {
		if (fileMode) {
			saveFile(AdHocRailway.this.actualFile);
		}
	}

	@Override
	public void addMenu(final JMenu menu) {
		menuBar.add(menu);
	}

	@Override
	public void addToolBar(final JToolBar toolbar) {
		toolbarPanel.add(toolbar);
	}

	@Override
	public void handleException(final Exception ex) {
		handleException(null, ex);

	}

	@Override
	public void handleException(final String message, final Exception e) {
		final ExceptionProcessor instance2 = ExceptionProcessor.getInstance();
		if (instance2 != null) {
			instance2.processException(message, e);
		}
	}

	@Override
	public void registerEscapeKey(final Action action) {
		final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		final JRootPane rootPane = getRootPane();
		rootPane.registerKeyboardAction(action, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	@Override
	public void addEditingModeListener(final EditingModeListener l) {
		editingModeListeners.add(l);
	}

	@Override
	public void commandDataReceived(final String response) {

	}

	@Override
	public void infoDataSent(final String infoData) {

	}

	@Override
	public void commandDataSent(final String commandData) {
		if (preferences.getBooleanValue(LOGGING)) {
			updateCommandHistory("To Server: " + commandData);
		}
		LOGGER.info("To Server: " + commandData.trim());
	}

	@Override
	public void infoDataReceived(final String infoData) {
		if (preferences.getBooleanValue(LOGGING)) {
			updateCommandHistory("From Server: " + infoData);
		}
		LOGGER.info("From Server: " + infoData.trim());
	}

	@Override
	public void editingModeChanged(final boolean editing) {
		switchesItem.setEnabled(editing);
		routesItem.setEnabled(editing);
		locomotivesItem.setEnabled(editing);
		turnoutsToolBarButton.setEnabled(editing);
		routesToolBarButton.setEnabled(editing);
		locomotivesToolBarButton.setEnabled(editing);

	}

	private void autoConnect() {

		try {
			if (preferences.getBooleanValue(SRCP_AUTOCONNECT)
					&& !preferences
							.getBooleanValue(PreferencesKeys.AUTO_DISCOVER_AND_CONNECT_SERVERS)) {
				new ConnectAction().actionPerformed(null);
			} else if (preferences
					.getBooleanValue(PreferencesKeys.AUTO_DISCOVER_AND_CONNECT_SERVERS)) {

				final JmDNS adhocServermDNS = JmDNS.create();
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

			}

		} catch (final IOException e) {
			handleException(e);
		}
	}

	private void setUpLogging() {
		PropertyConfigurator.configure("./etc/log4j.properties");

		final FileAppender appender = new FileAppender();
		appender.setName("MyFileAppender");
		appender.setLayout(new PatternLayout("%d [%t] %-5p %c{1} - %m%n"));
		String localhostname = "";
		try {
			localhostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			e.printStackTrace();
		}
		final String userName = System.getProperty("user.name");

		appender.setFile("./logs/" + localhostname + "_" + userName + ".log");
		appender.setAppend(true);
		appender.setThreshold(Level.DEBUG);
		appender.activateOptions();
		Logger.getRootLogger().addAppender(appender);

	}

	private void updateCommandHistory(final String text) {
		final DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
		final String date = df.format(Calendar.getInstance().getTime());
		final String fullText = "[" + date + "]: " + text;
		SwingUtilities.invokeLater(new CommandHistoryUpdater(fullText));
	}

	private void loadControlLayer() {
		initProceeded("Loading Control Layer (Power)");
		appContext.setPowerControl(SRCPPowerControl.getInstance());

		initProceeded("Loading Control Layer (Locomotives)");
		appContext.setLocomotiveControl(new SRCPLocomotiveControlAdapter());

		initProceeded("Loading Control Layer (Turnouts)");
		final SRCPTurnoutControlAdapter turnoutControl = new SRCPTurnoutControlAdapter();
		appContext.setTurnoutControl(turnoutControl);

		initProceeded("Loading Control Layer (Routes)");
		appContext.setRouteControl(new SRCPRouteControlAdapter(turnoutControl));

		initProceeded("Loading Control Layer (Locks)");
		appContext.setLockControl(SRCPLockControl.getInstance());
	}

	private void initGUI() {

		setFont(new Font("Verdana", Font.PLAIN, 19));
		setLayout(new BorderLayout());
		initMenu();
		initToolbar();
		statusBarPanel = initStatusBar();
		mainPanel = new JPanel();

		mainPanel = new JPanel(new MigLayout("", "[][grow]", "[grow][]"));

		final JPanel segmentPanel = new KeyControl(appContext);

		trackControlPanel = new TrackControlPanel(appContext);
		locomotiveControlPanel = new LocomotiveControlPanel(appContext);
		powerControlPanel = new PowerControlPanel(appContext);
		powerControlPanel.setConnected(false);

		mainPanel.add(segmentPanel, "grow");
		mainPanel.add(trackControlPanel, "grow, wrap");
		mainPanel.add(powerControlPanel, "grow");
		mainPanel.add(locomotiveControlPanel, "shrink 0, wrap");

		add(mainPanel, BorderLayout.CENTER);
		add(statusBarPanel, BorderLayout.PAGE_END);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				new ExitAction().actionPerformed(null);
			}
		});
		hostnameLabel.setText(preferences
				.getStringValue(PreferencesKeys.SRCP_HOSTNAME));
		addEditingModeListener(this);
		for (final EditingModeListener l : editingModeListeners) {
			l.editingModeChanged(appContext.isEditingMode());
		}
	}

	private void updateGUI() {
		disableNavigationKeys(mainPanel);
		mainPanel.requestFocus();
	}

	private void loadPersistenceLayer() {

		final boolean useAdHocServer = preferences
				.getBooleanValue(PreferencesKeys.USE_ADHOC_SERVER);

		initProceeded("Loading Persistence Layer (Locomotives)");
		LocomotiveManager locomotiveManager = appContext.getLocomotiveManager();
		if (locomotiveManager == null) {
			locomotiveManager = new LocomotiveManagerImpl();
			appContext.setLocomotiveManager(locomotiveManager);
		}

		locomotiveManager.setLocomotiveControl(appContext
				.getLocomotiveControl());
		if (useAdHocServer) {
			locomotiveManager.setLocomotiveService(new SIOLocomotiveService());
		} else {
			locomotiveManager.setLocomotiveService(new XMLLocomotiveService());
		}

		locomotiveManager.initialize();

		initProceeded("Loading Persistence Layer (Turnouts)");

		TurnoutManager turnoutManager = appContext.getTurnoutManager();
		if (turnoutManager == null) {
			turnoutManager = new TurnoutManagerImpl();
		}
		appContext.setTurnoutManager(turnoutManager);
		turnoutManager.setTurnoutControl(appContext.getTurnoutControl());
		if (useAdHocServer) {
			turnoutManager.setTurnoutService(new SIOTurnoutService());
		} else {
			turnoutManager.setTurnoutService(new XMLTurnoutService());
		}
		turnoutManager.initialize();

		initProceeded("Loading Persistence Layer (Routes)");
		RouteManager routeManager = appContext.getRouteManager();

		if (routeManager == null) {
			routeManager = new RouteManagerImpl(turnoutManager);
		}
		appContext.setRouteManager(routeManager);
		routeManager.setRouteControl(appContext.getRouteControl());
		if (useAdHocServer) {
			routeManager.setRouteService(new SIORouteService());
		} else {
			routeManager.setRouteService(new XMLRouteService());
		}
		routeManager.initialize();

		if (useAdHocServer
				&& !preferences
						.getBooleanValue(PreferencesKeys.AUTO_DISCOVER_AND_CONNECT_SERVERS)) {

			final String url = getAdHocServerURL();

			connectToAdHocServer(url);

		}
	}

	private String getAdHocServerURL() {
		final StringBuilder b = new StringBuilder();
		b.append("http://");

		b.append(preferences
				.getStringValue(PreferencesKeys.ADHOC_SERVER_HOSTNAME));
		b.append(":");
		b.append(preferences.getStringValue(PreferencesKeys.ADHOC_SERVER_PORT));
		final String url = b.toString();
		return url;
	}

	private void connectToAdHocServer(final String url) {
		SIOService.getInstance().connect(url, new ServiceListener() {

			@Override
			public void disconnected() {
				updateCommandHistory("Successfully connected to AdHoc-Server");
			}

			@Override
			public void connectionError(final Exception ex) {
				updateCommandHistory("Connection error: " + ex.getMessage());
				preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER,
						false);
				try {
					preferences.save();
				} catch (final IOException e) {
					handleException(e);
				}
				handleException(ex);
			}

			@Override
			public void connected() {
				setTitle(AdHocRailway.TITLE + " [" + url + "]");

				updateCommandHistory("Successfully connected to AdHoc-Server: "
						+ url);

			}
		});
	}

	private void disconnectFromCurrentPersistence() {
		appContext.getTurnoutManager().disconnect();
		appContext.getRouteManager().disconnect();
		appContext.getLocomotiveManager().disconnect();
	}

	private void disableNavigationKeys(final Component comp) {
		comp.setFocusTraversalKeysEnabled(false);
		if (comp instanceof Container) {
			final Component[] children = ((Container) comp).getComponents();
			for (int i = 0; i < children.length; i++) {
				disableNavigationKeys(children[i]);
			}
		}
	}

	private void initProceeded(final String message) {
		splash.nextStep(message);
	}

	private void switchToFileMode() {
		fileMode = true;
		preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, false);
		try {
			preferences.save();
		} catch (final IOException e) {
			handleException(e);
		}
	}

	private void switchToServerMode() {
		fileMode = false;
		preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, true);
		try {
			preferences.save();
		} catch (final IOException e) {
			handleException(e);
		}
	}

	private void disableEnableMenuItems() {
		saveAsItem.setEnabled(fileMode);
		saveItem.setEnabled(fileMode);

	}

	private void saveFile(final File file) {
		try {
			final XMLServiceHelper xmlService = new XMLServiceHelper();
			xmlService.saveFile(appContext, file);

		} catch (final IOException e) {

			handleException(e);
		}
		updateCommandHistory("AdHoc-Railway Configuration saved (" + file + ")");
	}

	private void initMenu() {
		menuBar = new JMenuBar();
		/* FILE */
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		final JMenuItem newItem = new JMenuItem(new NewFileAction());
		newItem.setMnemonic(KeyEvent.VK_N);
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));

		final JMenuItem openItem = new JMenuItem(new OpenFileAction());
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));

		final JMenuItem openDatabaseItem = new JMenuItem(
				new OpenDatabaseAction());

		saveItem = new JMenuItem(new SaveAction());
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));

		saveAsItem = new JMenuItem(new SaveAsAction());
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));

		final JMenuItem importLocomotivesItem = new JMenuItem(
				new ImportLocomotivesAction());
		final JMenuItem exportLocomotivesItem = new JMenuItem(
				new ExportLocomotivesAction());

		final JMenu importMenu = new JMenu("Import");
		importMenu.add(importLocomotivesItem);
		final JMenu exportMenu = new JMenu("Export");
		exportMenu.add(exportLocomotivesItem);

		final JMenuItem clearLocomotivesItem = new JMenuItem(
				new ClearLocomotivesAction());
		final JMenuItem clearTurnoutsRoutesItem = new JMenuItem(
				new ClearTurnoutsAndRoutesAction());

		final JMenu clearMenu = new JMenu("Clear");
		clearMenu.add(clearLocomotivesItem);
		clearMenu.add(clearTurnoutsRoutesItem);

		final JMenuItem exitItem = new JMenuItem(new ExitAction());
		exitItem.setMnemonic(KeyEvent.VK_X);
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));

		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(openDatabaseItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(importMenu);
		fileMenu.add(exportMenu);
		fileMenu.add(clearMenu);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitItem);

		/* EDIT */
		final JMenu editMenu = new JMenu("Edit");
		enableEditing = new JCheckBoxMenuItem(new EnableEditingAction());

		switchesItem = new JMenuItem(new TurnoutAction());
		routesItem = new JMenuItem(new RoutesAction());
		locomotivesItem = new JMenuItem(new LocomotivesAction());
		final JMenuItem preferencesItem = new JMenuItem(new PreferencesAction());
		editMenu.setMnemonic(KeyEvent.VK_E);
		switchesItem.setMnemonic(KeyEvent.VK_S);
		switchesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.ALT_MASK));
		routesItem.setMnemonic(KeyEvent.VK_R);
		routesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.ALT_MASK));
		locomotivesItem.setMnemonic(KeyEvent.VK_L);
		locomotivesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.ALT_MASK));
		preferencesItem.setMnemonic(KeyEvent.VK_P);
		preferencesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.ALT_MASK));
		editMenu.add(enableEditing);
		editMenu.add(new JSeparator());
		editMenu.add(switchesItem);
		editMenu.add(routesItem);
		editMenu.add(locomotivesItem);
		editMenu.add(new JSeparator());
		editMenu.add(preferencesItem);

		/* DAEMON */
		final JMenu daemonMenu = new JMenu("SRCP");
		daemonConnectItem = new JMenuItem(new ConnectAction());
		daemonConnectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		daemonDisconnectItem = new JMenuItem(new DisconnectAction());
		daemonPowerOnItem = new JMenuItem(new PowerOnAction());
		assignAccelerator(daemonPowerOnItem, "PowerOn");
		daemonPowerOnItem.setEnabled(true);
		daemonPowerOffItem = new JMenuItem(new PowerOffAction());
		assignAccelerator(daemonPowerOffItem, "PowerOff");
		daemonPowerOffItem.setEnabled(true);
		daemonDisconnectItem.setEnabled(false);
		daemonMenu.add(daemonConnectItem);
		daemonMenu.add(daemonDisconnectItem);
		daemonMenu.add(new JSeparator());
		daemonMenu.add(daemonPowerOnItem);
		daemonMenu.add(daemonPowerOffItem);
		daemonMenu.add(new JSeparator());

		/* VIEW */
		final JMenu viewMenu = new JMenu("View");
		final JMenuItem refreshItem = new JMenuItem(new RefreshAction());
		final JMenuItem fullscreenItem = new JMenuItem(
				new ToggleFullscreenAction());

		viewMenu.add(refreshItem);
		viewMenu.add(fullscreenItem);

		/* HELP */
		// JMenu helpMenu = new JMenu("Help");
		addMenu(fileMenu);
		addMenu(editMenu);
		addMenu(daemonMenu);
		addMenu(viewMenu);
		// addMenu(helpMenu);
		setJMenuBar(menuBar);
	}

	/**
	 * 
	 */
	private void assignAccelerator(final JMenuItem item, final String actionName) {
		final Set<KeyStroke> strokes = preferences.getKeyBoardLayout().getKeys(
				actionName);
		if (strokes != null && strokes.size() > 0) {
			item.setAccelerator(strokes.iterator().next());
		}
	}

	private void initToolbar() {
		/* FILE */
		final JToolBar fileToolBar = new JToolBar();

		final JButton newFileToolBarButton = new SmallToolbarButton(
				new NewFileAction());
		final JButton openFileToolBarButton = new SmallToolbarButton(
				new OpenFileAction());
		final JButton openDatabaseToolBarButton = new SmallToolbarButton(
				new OpenDatabaseAction());
		final JButton saveToolBarButton = new SmallToolbarButton(
				new SaveAction());
		final JButton exitToolBarButton = new SmallToolbarButton(
				new ExitAction());

		fileToolBar.add(newFileToolBarButton);
		fileToolBar.add(openFileToolBarButton);
		fileToolBar.add(openDatabaseToolBarButton);
		fileToolBar.add(saveToolBarButton);
		fileToolBar.add(exitToolBarButton);

		/* DIGITAL */
		final JToolBar digitalToolBar = new JToolBar();
		turnoutsToolBarButton = new SmallToolbarButton(new TurnoutAction());
		routesToolBarButton = new SmallToolbarButton(new RoutesAction());
		locomotivesToolBarButton = new SmallToolbarButton(
				new LocomotivesAction());
		final JButton preferencesToolBarButton = new SmallToolbarButton(
				new PreferencesAction());

		digitalToolBar.add(turnoutsToolBarButton);
		digitalToolBar.add(routesToolBarButton);
		digitalToolBar.add(locomotivesToolBarButton);
		digitalToolBar.add(preferencesToolBarButton);

		/* DAEMON */
		final JToolBar daemonToolBar = new JToolBar();
		hostnameLabel = new JLabel();
		hostnameLabel.setText(preferences
				.getStringValue(PreferencesKeys.SRCP_HOSTNAME));
		connectToolBarButton = new SmallToolbarButton(new ConnectAction());
		disconnectToolBarButton = new SmallToolbarButton(new DisconnectAction());
		disconnectToolBarButton.setEnabled(false);

		daemonToolBar.add(hostnameLabel);
		daemonToolBar.addSeparator();
		daemonToolBar.add(connectToolBarButton);
		daemonToolBar.add(disconnectToolBarButton);

		/* VIEWS */
		final JToolBar viewToolBar = new JToolBar();
		final JButton refreshButton = new SmallToolbarButton(
				new RefreshAction());
		toggleFullscreenButton = new SmallToolbarButton(
				new ToggleFullscreenAction());

		viewToolBar.add(refreshButton);
		viewToolBar.add(toggleFullscreenButton);

		/* ERROR */
		final ErrorPanel errorPanel = new ErrorPanel();
		ExceptionProcessor.getInstance(errorPanel);

		toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		addToolBar(fileToolBar);
		addToolBar(digitalToolBar);
		addToolBar(daemonToolBar);
		addToolBar(viewToolBar);

		final JPanel toolbarErrorPanel = new JPanel(new BorderLayout(10, 10));
		toolbarErrorPanel.add(toolbarPanel, BorderLayout.WEST);
		toolbarErrorPanel.add(errorPanel, BorderLayout.EAST);

		add(toolbarErrorPanel, BorderLayout.PAGE_START);
	}

	private JPanel initStatusBar() {
		final JPanel statusBarPanel = new JPanel();
		commandHistoryModel = new DefaultComboBoxModel<String>();
		commandHistory = new JComboBox<String>(commandHistoryModel);
		commandHistory.setEditable(false);
		commandHistory.setFocusable(false);

		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);

		statusBarPanel.setLayout(new BorderLayout(5, 0));
		statusBarPanel.add(progressBar, BorderLayout.WEST);
		statusBarPanel.add(commandHistory, BorderLayout.CENTER);
		return statusBarPanel;
	}

	private void setSessionOnControllers(final SRCPSession session) {
		appContext.getPowerControl().setSession(session);
		((SRCPTurnoutControlAdapter) appContext.getTurnoutControl())
				.setSession(session);
		((SRCPLocomotiveControlAdapter) appContext.getLocomotiveControl())
				.setSession(session);
		((SRCPRouteControlAdapter) appContext.getRouteControl())
				.setSession(session);
		appContext.getLockControl().setSession(session);
	}

	private class CommandHistoryUpdater implements Runnable {

		private final String text;

		public CommandHistoryUpdater(final String text) {
			this.text = text;
		}

		@Override
		public void run() {
			if (commandHistoryModel.getSize() > 100) {
				commandHistoryModel.removeElementAt(100);
			}
			commandHistoryModel.insertElementAt(text, 0);
			commandHistory.setSelectedIndex(0);
		}
	}

	private class NewFileAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2206015736690123233L;

		public NewFileAction() {
			super("New\u2026", createImageIconFromIconSet("document-new.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			if (fileMode) {

				final int result = JOptionPane.showConfirmDialog(
						AdHocRailway.this,
						"Do you want to save the actual configuration?",
						"Export to database", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						createImageIconFromIconSet("dialog-information.png"));
				if (result == JOptionPane.YES_OPTION) {
					saveActualFile();
				}
			}
			disconnectFromCurrentPersistence();

			switchToFileMode();

			loadPersistenceLayer();

			appContext.getLocomotiveManager().clear();
			appContext.getTurnoutManager().clear();
			appContext.getRouteManager().clear();

			disableEnableMenuItems();

			hostnameLabel.setText(Preferences.getInstance().getStringValue(
					PreferencesKeys.SRCP_HOSTNAME));
			setTitle(AdHocRailway.TITLE + " []");
			actualFile = null;
			updateGUI();
			updateCommandHistory("Empty AdHoc-Railway Configuration created");
		}

	}

	private class OpenFileAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3333376253277589231L;

		public OpenFileAction() {
			super("Open\u2026", createImageIconFromIconSet("document-open.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser fileChooser = new JFileChooser(new File("."));
			final int returnVal = fileChooser.showOpenDialog(AdHocRailway.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				actualFile = fileChooser.getSelectedFile();
				openFile(actualFile);
			} else {
				updateCommandHistory("Open command cancelled by user");
			}

		}

		public void openFile(final File file) {
			final Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {

						disconnectFromCurrentPersistence();

						progressBar.setIndeterminate(true);

						switchToFileMode();

						loadPersistenceLayer();

						appContext.getLocomotiveManager().clear();
						appContext.getTurnoutManager().clear();
						appContext.getRouteManager().clear();

						disableEnableMenuItems();

						new XMLServiceHelper().loadFile(appContext, file);

						setTitle(AdHocRailway.TITLE + " ["
								+ file.getAbsolutePath() + "]");
						AdHocRailway.this.actualFile = file;
						updateGUI();
						updateCommandHistory("AdHoc-Railway Configuration loaded ("
								+ file + ")");
					} catch (final ConfigurationException e) {
						handleException(e);
					}
					progressBar.setIndeterminate(false);

				}
			});
			t.start();
		}
	}

	private class OpenDatabaseAction extends AbstractAction {

		private static final long serialVersionUID = -4287132162321389954L;

		public OpenDatabaseAction() {
			super("Open Database\u2026",
					createImageIconFromIconSet("network-server-database.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			try {
				progressBar.setIndeterminate(true);

				disconnectFromCurrentPersistence();
				switchToServerMode();
				loadPersistenceLayer();
				disableEnableMenuItems();

			} catch (final PersistenceException ex) {

				handleException("Failed to connect to database", ex);
			}
			updateGUI();
			progressBar.setIndeterminate(false);
		}
	}

	private class ClearLocomotivesAction extends AbstractAction {

		private static final long serialVersionUID = -4287132162321389954L;

		public ClearLocomotivesAction() {
			super("Clear Locomotives\u2026",
					createImageIconFromIconSet("list-remove.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			try {
				progressBar.setIndeterminate(true);
				final int result = JOptionPane.showConfirmDialog(
						AdHocRailway.this,
						"Do you REALLY want to remove all locomotives?",
						"Export to database", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						createImageIconFromIconSet("dialog-warning.png"));
				if (result == JOptionPane.YES_OPTION) {
					appContext.getLocomotiveManager().clearToService();
				}

			} catch (final PersistenceException ex) {
				handleException("Failed to connect to database", ex);
			}
			updateGUI();
			progressBar.setIndeterminate(false);
		}
	}

	private class ClearTurnoutsAndRoutesAction extends AbstractAction {

		private static final long serialVersionUID = -4287132162321389954L;

		public ClearTurnoutsAndRoutesAction() {
			super("Clear Turnouts and Routes\u2026",
					createImageIconFromIconSet("list-remove.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			try {
				progressBar.setIndeterminate(true);
				final int result = JOptionPane
						.showConfirmDialog(
								AdHocRailway.this,
								"Do you REALLY want to remove all turnouts and routes?",
								"Export to database",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								createImageIconFromIconSet("dialog-warning.png"));
				if (result == JOptionPane.YES_OPTION) {
					appContext.getTurnoutManager().clearToService();
					appContext.getRouteManager().clearToService();
				}

			} catch (final PersistenceException ex) {
				handleException("Failed to connect to database", ex);
			}
			updateGUI();
			progressBar.setIndeterminate(false);
		}
	}

	private class SaveAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3017204569992757846L;

		public SaveAction() {
			super("Save", createImageIconFromIconSet("document-save.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (actualFile == null) {
				new SaveAsAction().actionPerformed(null);
			}
			saveActualFile();
		}
	}

	private class SaveAsAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4841045364461725101L;

		public SaveAsAction() {
			super("Save as\u2026",
					createImageIconFromIconSet("document-save-as.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			final JFileChooser fileChooser = new JFileChooser(new File("."));
			final int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File saveFile = fileChooser.getSelectedFile();
				saveFile(saveFile);
			} else {
				updateCommandHistory("Save command cancelled by user");
			}

		}
	}

	private class ImportLocomotivesAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6274805581979740341L;

		public ImportLocomotivesAction() {
			super("Import Locomotives");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			progressBar.setIndeterminate(true);
			final JFileChooser fileChooser = new JFileChooser(new File("."));
			final int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				new XMLServiceHelper().importLocomotivesFromFile(
						fileChooser.getSelectedFile(),
						appContext.getLocomotiveManager());
			}

			progressBar.setIndeterminate(false);
		}
	}

	private class ExportLocomotivesAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2387864940540796841L;

		public ExportLocomotivesAction() {
			super("Export Locomotives");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser fileChooser = new JFileChooser(new File("."));
			final int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					new XMLServiceHelper().exportLocomotivesToFile(
							fileChooser.getSelectedFile(),
							appContext.getLocomotiveManager());
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private class ExitAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1614667243269636455L;

		public ExitAction() {
			super("Exit", createImageIconFromIconSet("application-exit.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			try {
				SRCPLockControl.getInstance().releaseAllLocks();
				appContext.getLocomotiveControl().emergencyStopActiveLocos();
			} catch (final SRCPLockingException e1) {
				handleException(e1);
			} catch (final LocomotiveException e1) {
				handleException(e1);
			}

			if (actualFile != null) {
				preferences.setStringValue(PreferencesKeys.LAST_OPENED_FILE,
						actualFile.getAbsolutePath());
				try {
					preferences.save();
				} catch (final IOException e1) {
					handleException(e1);
				}
			}
			if (!fileMode) {
				disconnectFromCurrentPersistence();
			}
			System.exit(0);
		}
	}

	private class TurnoutAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6752957708659838960L;

		public TurnoutAction() {
			super("Turnouts\u2026", createImageIconFromIconSet("switch.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final TurnoutConfigurationDialog switchConfigDialog = new TurnoutConfigurationDialog(
					AdHocRailway.this, appContext);
			if (switchConfigDialog.isOkPressed()) {
				updateCommandHistory("Turnout configuration changed");
			}
		}
	}

	private class RoutesAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 376791842370116533L;

		public RoutesAction() {
			super("Routes\u2026", createImageIconFromIconSet("route_edit.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			new RoutesConfigurationDialog(AdHocRailway.this, appContext);
		}
	}

	private class LocomotivesAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 534491281012259216L;

		public LocomotivesAction() {
			super("Locomotives\u2026",
					createImageIconFromIconSet("locomotive.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final LocomotiveConfigurationDialog locomotiveConfigDialog = new LocomotiveConfigurationDialog(
					appContext, AdHocRailway.this);
			if (locomotiveConfigDialog.isOkPressed()) {
				updateCommandHistory("Locomotive configuration changed");
			}
		}
	}

	private class PreferencesAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 820579148477344864L;

		public PreferencesAction() {
			super("Preferences\u2026",
					createImageIconFromIconSet("preferences-system.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final PreferencesDialog p = new PreferencesDialog(
					AdHocRailway.this, appContext);
			if (p.isOkPressed()) {
				updateGUI();
				hostnameLabel.setText(preferences
						.getStringValue(PreferencesKeys.SRCP_HOSTNAME));
				updateCommandHistory("Preferences saved to: "
						+ preferences.getConfigFile());
			}
		}
	}

	/**
	 * Handels the start of a connection with the srcpd-server.
	 * 
	 * @author fork
	 */
	private class ConnectAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2924306088384462135L;

		public ConnectAction() {
			super("Connect", createImageIconFromIconSet("daemonconnect.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String host = preferences.getStringValue(SRCP_HOSTNAME);
			final int port = preferences.getIntValue(SRCP_PORT);

			connectToSRCPServer(host, port);
		}
	}

	private class DisconnectAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 947951249636462131L;

		public DisconnectAction() {
			super("Disconnect",
					createImageIconFromIconSet("daemondisconnect.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				final String host = preferences.getStringValue(SRCP_HOSTNAME);
				final int port = preferences.getIntValue(SRCP_PORT);

				appContext.getLocomotiveControl().emergencyStopActiveLocos();
				SRCPSession session = appContext.getSession();
				session.disconnect();
				session = null;

				setSessionOnControllers(session);

				enableDisableDueToSessionState(false);
				updateCommandHistory("Disconnected from server " + host
						+ " on port " + port);
			} catch (final SRCPException e1) {
				handleException(e1);
			} catch (final LocomotiveException e1) {
				e1.printStackTrace();
			}
		}

	}

	private class PowerOnAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4908838367753105920L;

		public PowerOnAction() {
			super("Power On");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener
		 * #actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			try {
				appContext.getPowerControl().setAllStates(SRCPPowerState.ON);
			} catch (final SRCPPowerSupplyException e) {
				handleException(e);
			} catch (final SRCPModelException e) {
				handleException(e);
			}
		}
	}

	private class PowerOffAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 834003379948714322L;

		public PowerOffAction() {
			super("Power Off");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener
		 * #actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			try {
				appContext.getPowerControl().setAllStates(SRCPPowerState.OFF);
			} catch (final SRCPPowerSupplyException e) {
				handleException(e);
			} catch (final SRCPModelException e) {
				handleException(e);
			}
		}
	}

	private class RefreshAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6402433986415682675L;

		public RefreshAction() {
			super("Refresh", createImageIconFromIconSet("view-refresh.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			disconnectFromCurrentPersistence();
			loadPersistenceLayer();
		}
	}

	private class ToggleFullscreenAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1610381669528977741L;

		public ToggleFullscreenAction() {
			super("ToggleFullscreen",
					createImageIconFromIconSet("view-fullscreen.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (fullscreen) {
				dispose();
				menuBar.setVisible(true);
				setResizable(true);
				setUndecorated(false);
				setSize(1000, 700);
				setVisible(true);
				toggleFullscreenButton
						.setIcon(createImageIconFromIconSet("view-fullscreen.png"));
				fullscreen = false;
			} else {
				dispose();
				menuBar.setVisible(false);
				setResizable(false);
				setUndecorated(true);
				setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
				setVisible(true);
				toggleFullscreenButton
						.setIcon(createImageIconFromIconSet("view-fullscreen.png"));
				fullscreen = true;
			}
		}
	}

	private class EnableEditingAction extends AbstractAction {

		private static final long serialVersionUID = 2409376830847967919L;

		public EnableEditingAction() {
			super("Edit mode");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final boolean editingMode = enableEditing.isSelected();
			appContext.setEditingMode(editingMode);

			for (final EditingModeListener l : editingModeListeners) {
				l.editingModeChanged(editingMode);
			}
		}
	}

	private void enableDisableDueToSessionState(final boolean connected) {
		daemonConnectItem.setEnabled(!connected);
		daemonDisconnectItem.setEnabled(connected);
		connectToolBarButton.setEnabled(!connected);
		disconnectToolBarButton.setEnabled(connected);
		powerControlPanel.setConnected(connected);
	}

	private void connectToSRCPServer(final String host, final int port) {
		try {
			final SRCPSession session = new SRCPSession(host, port, false);
			appContext.setSession(session);
			session.getCommandChannel().addCommandDataListener(
					AdHocRailway.this);
			session.getInfoChannel().addInfoDataListener(AdHocRailway.this);
			setSessionOnControllers(session);
			session.connect();
			enableDisableDueToSessionState(true);

			updateCommandHistory("Connected to server " + host + " on port "
					+ port);
		} catch (final SRCPException e) {
			preferences
					.setBooleanValue(PreferencesKeys.SRCP_AUTOCONNECT, false);
			try {
				preferences.save();
			} catch (final IOException e2) {
				handleException("Server not running", e2);
			}
			handleException("SRCP server not running", e);
		}

	}

	public static void main(final String[] args) {
		if (args.length == 1) {
			new AdHocRailway(args[0]);
		} else {
			new AdHocRailway();
		}
	}
}
