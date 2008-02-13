/*------------------------------------------------------------------------
 * 
 * <./ui/AdHocRailway.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:30 BST 2006
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

package ch.fork.AdHocRailway.ui;

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.locking.SRCPLockControl;
import ch.fork.AdHocRailway.domain.locking.exception.LockingException;
import ch.fork.AdHocRailway.domain.locomotives.HibernateLocomotivePersistence;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.domain.locomotives.MemoryLocomotivePersistence;
import ch.fork.AdHocRailway.domain.locomotives.SRCPLocomotiveControl;
import ch.fork.AdHocRailway.domain.routes.HibernateRoutePersistence;
import ch.fork.AdHocRailway.domain.routes.MemoryRoutePersistence;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.routes.SRCPRouteControl;
import ch.fork.AdHocRailway.domain.turnouts.HibernateTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.MemoryTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.technical.configuration.ConfigurationException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.technical.configuration.exporter.XMLExporter_0_3;
import ch.fork.AdHocRailway.technical.configuration.importer.XMLImporter;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveControlPanel;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfigurationDialog;
import ch.fork.AdHocRailway.ui.routes.configuration.RoutesConfigurationDialog;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfigurationDialog;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

public class AdHocRailway extends JFrame implements CommandDataListener,
		InfoDataListener, PreferencesKeys {
	private static Logger				logger				= Logger
																	.getLogger(AdHocRailway.class);
	private static final long			serialVersionUID	= 1L;
	private static AdHocRailway			instance;

	private static final String			TITLE				= "AdHoc-Railway";

	private SRCPSession					session;

	private TurnoutControlIface			turnoutControl;

	private TurnoutPersistenceIface		turnoutPersistence;

	private LocomotiveControlface		locomotiveControl;

	private LocomotivePersistenceIface	locomotivePersistence;

	private SRCPLockControl				lockControl;

	private RouteControlIface			routeControl;

	private Preferences					preferences;

	private TrackControlPanel			trackControlPanel;

	private LocomotiveControlPanel		locomotiveControlPanel;

	private JPanel						statusBarPanel;

	private JLabel						hostnameLabel;

	private JButton						connectToolBarButton;

	private JButton						disconnectToolBarButton;

	private JComboBox					commandHistory;

	private DefaultComboBoxModel		commandHistoryModel;

	private JMenuItem					daemonConnectItem;

	private JMenuItem					daemonDisconnectItem;

	private JMenuItem					daemonResetItem;

	private JButton						toggleFullscreenButton;

	private JMenuBar					menuBar;

	boolean								fullscreen			= false;

	private SplashWindow				splash;

	private JTabbedPane					trackControl;

	private JPanel						mainPanel;
	private JPanel						toolbarPanel;
	public File							file;
	private RoutePersistenceIface		routePersistence;

	public AdHocRailway() {
		this(null);
	}

	public AdHocRailway(String file) {
		super(TITLE);

		instance = this;
		splash = new SplashWindow(createImageIcon("splash.png"), this, 500, 10);
		setIconImage(createImageIcon("RailControl.png").getImage());

		initProceeded("Loading Persistence Layer (Preferences)");
		preferences = Preferences.getInstance();
		boolean useDatabase = preferences
				.getBooleanValue(PreferencesKeys.USE_DATABASE);

		initProceeded("Loading Persistence Layer (Locomotives)");
		if (useDatabase)
			locomotivePersistence = HibernateLocomotivePersistence
					.getInstance();
		else
			locomotivePersistence = MemoryLocomotivePersistence.getInstance();

		initProceeded("Loading Persistence Layer (Turnouts)");
		if (useDatabase)
			turnoutPersistence = HibernateTurnoutPersistence.getInstance();
		else
			turnoutPersistence = MemoryTurnoutPersistence.getInstance();

		initProceeded("Loading Persistence Layer (Routes)");
		if (useDatabase)
			routePersistence = HibernateRoutePersistence.getInstance();
		else
			routePersistence = MemoryRoutePersistence.getInstance();

		if (useDatabase) {
			String host = preferences
					.getStringValue(PreferencesKeys.DATABASE_HOST);
			String database = preferences
					.getStringValue(PreferencesKeys.DATABASE_NAME);
			String url = "jdbc:mysql://" + host + "/" + database;
			setTitle(AdHocRailway.TITLE + " [" + url + "]");
		}
		initProceeded("Loading Control Layer (Locomotives)");
		locomotiveControl = SRCPLocomotiveControl.getInstance();
		locomotiveControl.setLocomotivePersistence(locomotivePersistence);

		initProceeded("Loading Control Layer (Turnouts)");
		turnoutControl = SRCPTurnoutControl.getInstance();
		turnoutControl.setTurnoutPersistence(turnoutPersistence);

		initProceeded("Loading Control Layer (Routes)");
		routeControl = SRCPRouteControl.getInstance();
		routeControl.setRoutePersistence(routePersistence);

		initProceeded("Loading Control Layer (Locks)");
		lockControl = SRCPLockControl.getInstance();

		initProceeded("Creating GUI ...");
		initGUI();

		trackControlPanel.update();
		locomotiveControlPanel.update();

		initKeyboardActions();

		setSize(1000, 700);

		TutorialUtils.locateOnOpticalScreenCenter(this);

		initProceeded("RailControl started");
		updateCommandHistory("RailControl started");
		String lastFile = preferences.getStringValue(LAST_OPENED_FILE);
		if (lastFile != null && !lastFile.equals("") && !useDatabase) {

			new OpenAction().openFile(new File(preferences
					.getStringValue(LAST_OPENED_FILE)));
		}
		if (preferences.getBooleanValue(AUTOCONNECT))
			new ConnectAction().actionPerformed(null);
		setVisible(true);
	}

	public static AdHocRailway getInstance() {
		return instance;
	}

	private void initGUI() {

		setFont(new Font("Verdana", Font.PLAIN, 19));
		setLayout(new BorderLayout());

		initMenu();
		initToolbar();
		statusBarPanel = initStatusBar();
		add(statusBarPanel, BorderLayout.SOUTH);
		mainPanel = new JPanel();

		mainPanel = new JPanel(new BorderLayout());

		trackControlPanel = new TrackControlPanel();
		locomotiveControlPanel = new LocomotiveControlPanel();

		mainPanel.add(trackControlPanel, BorderLayout.CENTER);
		mainPanel.add(locomotiveControlPanel, BorderLayout.SOUTH);
		add(mainPanel, BorderLayout.CENTER);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				new ExitAction().actionPerformed(null);
			}
		});
		hostnameLabel.setText(preferences.getStringValue("Hostname"));

	}

	private void initKeyboardActions() {
		if (trackControl != null) {
			trackControl.registerKeyboardAction(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					if (trackControl.getSelectedIndex() == 0)
						trackControl.setSelectedIndex(1);
					else
						trackControl.setSelectedIndex(0);
				}

			}, "", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
		}
	}

	private void updateGUI() {
		updateTurnouts();
		updateLocomotives();
	}

	private void updateLocomotives() {
		locomotiveControl.update();
		locomotiveControlPanel.update();
	}

	private void updateTurnouts() {
		turnoutControl.update();
		trackControlPanel.update();
	}

	public void commandDataSent(String commandData) {
		if (preferences.getBooleanValue(LOGGING)) {
			updateCommandHistory("To Server: " + commandData);
		}
		logger.debug("To Server: " + commandData.trim());
	}

	public void infoDataReceived(String infoData) {
		if (preferences.getBooleanValue(LOGGING)) {
			updateCommandHistory("From Server: " + infoData);
		}
		logger.debug("From Server: " + infoData.trim());
	}

	public void updateCommandHistory(String text) {
		DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
		String date = df.format(GregorianCalendar.getInstance().getTime());
		String fullText = "[" + date + "]: " + text;
		SwingUtilities.invokeLater(new CommandHistoryUpdater(fullText));

	}

	private void initProceeded(String message) {
		logger.info(message);
		splash.nextStep(message);
	}

	private class CommandHistoryUpdater implements Runnable {

		private String	text;

		public CommandHistoryUpdater(String text) {
			this.text = text;
		}

		public void run() {
			if (commandHistoryModel.getSize() > 100) {
				commandHistoryModel.removeElementAt(100);
			}
			commandHistoryModel.insertElementAt(text, 0);
			commandHistory.setSelectedIndex(0);
		}
	}

	private class OpenAction extends AbstractAction {

		public OpenAction() {
			super("Open\u2026", createImageIcon("fileopen.png"));
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser(new File("."));
			int returnVal = fileChooser.showOpenDialog(AdHocRailway.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fileChooser.getSelectedFile();
				openFile(file);

			} else {
				updateCommandHistory("Open command cancelled by user");
			}

		}

		public void openFile(File file) {
			try {
				new XMLImporter(file.getAbsolutePath());
			} catch (ConfigurationException e) {
				ExceptionProcessor.getInstance().processException(e);
			}

			hostnameLabel.setText(Preferences.getInstance().getStringValue(
					PreferencesKeys.HOSTNAME));
			turnoutPersistence = MemoryTurnoutPersistence.getInstance();
			turnoutControl.setTurnoutPersistence(turnoutPersistence);
			locomotivePersistence = MemoryLocomotivePersistence.getInstance();
			locomotiveControl.setLocomotivePersistence(locomotivePersistence);
			setTitle(AdHocRailway.TITLE + " [" + file.getAbsolutePath() + "]");
			updateGUI();

		}
	}

	private class SaveAction extends AbstractAction {
		public SaveAction() {
			super("Save\u2026", createImageIcon("filesave.png"));
		}

		public void actionPerformed(ActionEvent e) {

			JFileChooser fileChooser = new JFileChooser(new File("."));
			int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveFile = fileChooser.getSelectedFile();

				try {
					XMLExporter_0_3 exporter = new XMLExporter_0_3(
							turnoutPersistence, locomotivePersistence,
							routePersistence);
					String xml = exporter.export();
					FileWriter fw = new FileWriter(saveFile);
					fw.write(xml);
					fw.close();
				} catch (FileNotFoundException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				} catch (IOException e2) {
					ExceptionProcessor.getInstance().processException(e2);
				}
				file = saveFile;

			} else {
				updateCommandHistory("Save command cancelled by user");
			}

		}
	}

	private class ExportToDatabaseAction extends AbstractAction {

		public ExportToDatabaseAction() {
			super("Export to Database");
		}

		public void actionPerformed(ActionEvent e) {
			int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
					"All data in the database will be deleted prior "
							+ "to the export. The application will afterwards "
							+ "switch to database-mode.\n"
							+ "Do you really want to proceed ?",
					"Export to database", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					createImageIcon("messagebox_warning.png"));
			if (result == JOptionPane.YES_OPTION) {
				try {
					new XMLImporter(file.getAbsolutePath(),
							HibernateTurnoutPersistence.getInstance(),
							HibernateLocomotivePersistence.getInstance(),
							HibernateRoutePersistence.getInstance());
				} catch (ConfigurationException e1) {
				}

				hostnameLabel.setText(Preferences.getInstance().getStringValue(
						PreferencesKeys.HOSTNAME));
				turnoutPersistence = HibernateTurnoutPersistence.getInstance();
				turnoutControl.setTurnoutPersistence(turnoutPersistence);
				locomotivePersistence = HibernateLocomotivePersistence
						.getInstance();
				locomotiveControl
						.setLocomotivePersistence(locomotivePersistence);
				routePersistence = HibernateRoutePersistence.getInstance();
				routeControl.setRoutePersistence(routePersistence);
				String host = preferences
						.getStringValue(PreferencesKeys.DATABASE_HOST);
				String database = preferences
						.getStringValue(PreferencesKeys.DATABASE_NAME);
				String url = "jdbc:mysql://" + host + "/" + database;
				setTitle(AdHocRailway.TITLE + " [" + url + "]");
				file = null;
				updateGUI();
			}
		}
	}

	private class ExitAction extends AbstractAction {

		public ExitAction() {
			super("Exit", createImageIcon("exit.png"));
		}

		public void actionPerformed(ActionEvent e) {
			int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
					"Really exit ?", "Exit", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					createImageIcon("messagebox_warning.png"));
			if (result == JOptionPane.YES_OPTION) {

				try {
					SRCPLockControl.getInstance().releaseAllLocks();
				} catch (LockingException e1) {
					e1.printStackTrace();
				}

				if (file != null) {
					preferences.setStringValue(
							PreferencesKeys.LAST_OPENED_FILE, file
									.getAbsolutePath());
					try {
						preferences.save();
					} catch (FileNotFoundException e1) {
						ExceptionProcessor.getInstance().processException(e1);
					} catch (IOException e1) {
						ExceptionProcessor.getInstance().processException(e1);
					}
				}
				System.exit(0);
			}
		}
	}

	private class TurnoutAction extends AbstractAction {

		public TurnoutAction() {
			super("Turnouts\u2026", createImageIcon("switch.png"));
		}

		public void actionPerformed(ActionEvent e) {
			TurnoutConfigurationDialog switchConfigDialog = new TurnoutConfigurationDialog(
					AdHocRailway.this);
			if (switchConfigDialog.isOkPressed()) {
				updateTurnouts();
				updateCommandHistory("Turnout configuration changed");
			}
		}
	}

	private class RoutesAction extends AbstractAction {

		public RoutesAction() {
			super("Routes\u2026", createImageIcon("route_edit.png"));
		}

		public void actionPerformed(ActionEvent e) {
			RoutesConfigurationDialog routesConfig = new RoutesConfigurationDialog(
					AdHocRailway.this);
			if (routesConfig.isOkPressed()) {
				updateTurnouts();
				updateCommandHistory("Routes configuration changed");
			}
		}
	}

	private class LocomotivesAction extends AbstractAction {

		public LocomotivesAction() {
			super("Locomotives\u2026", createImageIcon("locomotive.png"));
		}

		public void actionPerformed(ActionEvent e) {
			LocomotiveConfigurationDialog locomotiveConfigDialog = new LocomotiveConfigurationDialog(
					AdHocRailway.this);
			if (locomotiveConfigDialog.isOkPressed()) {
				updateLocomotives();
				updateCommandHistory("Locomotive configuration changed");
			}
		}
	}

	private class PreferencesAction extends AbstractAction {

		public PreferencesAction() {
			super("Preferences\u2026", createImageIcon("package_settings.png"));
		}

		public void actionPerformed(ActionEvent e) {
			PreferencesDialog p = new PreferencesDialog(AdHocRailway.this);
			if (p.isOkPressed()) {
				updateGUI();
				hostnameLabel.setText(preferences.getStringValue("Hostname"));
				updateCommandHistory("Preferences changed");
			}
		}
	}

	/**
	 * Handels the start of a connection with the srcpd-server.
	 * 
	 * @author fork
	 */
	private class ConnectAction extends AbstractAction {

		public ConnectAction() {
			super("Connect", createImageIcon("daemonconnect.png"));
		}

		public void actionPerformed(ActionEvent e) {
			try {
				String host = preferences.getStringValue(HOSTNAME);
				int port = preferences.getIntValue(PORT);
				session = new SRCPSession(host, port, false);
				session.getCommandChannel().addCommandDataListener(
						AdHocRailway.this);
				session.getInfoChannel().addInfoDataListener(AdHocRailway.this);
				((SRCPTurnoutControl) turnoutControl).setSession(session);
				((SRCPLocomotiveControl) locomotiveControl).setSession(session);
				// ((SRCPRouteControl) routeControl).setSession(session);
				lockControl.setSession(session);
				session.connect();
				daemonConnectItem.setEnabled(false);
				daemonDisconnectItem.setEnabled(true);
				daemonResetItem.setEnabled(true);
				connectToolBarButton.setEnabled(false);
				disconnectToolBarButton.setEnabled(true);
				// updateGUI();
				updateCommandHistory("Connected to server " + host
						+ " on port " + port);
			} catch (SRCPException e1) {
				if (e1.getCause() instanceof ConnectException) {
					ExceptionProcessor.getInstance().processException(
							"Server not running", e1);
				}
			}
		}
	}

	private class DisconnectAction extends AbstractAction {

		public DisconnectAction() {
			super("Disconnect", createImageIcon("daemondisconnect.png"));
		}

		public void actionPerformed(ActionEvent e) {
			try {
				String host = preferences.getStringValue(HOSTNAME);
				int port = preferences.getIntValue(PORT);
				session.disconnect();
				session = null;
				((SRCPTurnoutControl) turnoutControl).setSession(null);
				((SRCPLocomotiveControl) locomotiveControl).setSession(null);
				lockControl.setSession(null);
				daemonConnectItem.setEnabled(true);
				daemonDisconnectItem.setEnabled(false);
				daemonResetItem.setEnabled(false);
				connectToolBarButton.setEnabled(true);
				disconnectToolBarButton.setEnabled(false);
				updateCommandHistory("Disconnected from server " + host
						+ " on port " + port);
			} catch (SRCPException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}
	}

	private class ResetAction extends AbstractAction {

		public ResetAction() {
			super("Reset", createImageIcon("daemonreset.png"));
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class RefreshAction extends AbstractAction {

		public RefreshAction() {
			super("Refresh", createImageIcon("reload.png"));
		}

		public void actionPerformed(ActionEvent e) {
			updateGUI();
		}
	}

	private class ToggleFullscreenAction extends AbstractAction {

		public ToggleFullscreenAction() {
			super("ToggleFullscreen", createImageIcon("window_fullscreen.png"));
		}

		public void actionPerformed(ActionEvent e) {
			if (fullscreen) {
				dispose();
				menuBar.setVisible(true);
				setResizable(true);
				setUndecorated(false);
				setSize(1000, 700);
				setVisible(true);
				toggleFullscreenButton
						.setIcon(createImageIcon("window_fullscreen.png"));
				fullscreen = false;
			} else {
				dispose();
				menuBar.setVisible(false);
				setResizable(false);
				setUndecorated(true);
				setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
				setVisible(true);
				toggleFullscreenButton
						.setIcon(createImageIcon("window_nofullscreen.png"));
				fullscreen = true;
			}
		}
	}

	private void initMenu() {
		menuBar = new JMenuBar();
		/* FILE */
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem openItem = new JMenuItem(new OpenAction());
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));

		JMenuItem saveItem = new JMenuItem(new SaveAction());
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));

		JMenuItem exportToDatavaseItem = new JMenuItem(
				new ExportToDatabaseAction());
		exportToDatavaseItem.setMnemonic(KeyEvent.VK_O);
		exportToDatavaseItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		JMenuItem exitItem = new JMenuItem(new ExitAction());
		exitItem.setMnemonic(KeyEvent.VK_X);
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));

		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(exportToDatavaseItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitItem);

		/* EDIT */
		JMenu editMenu = new JMenu("Edit");
		JMenuItem switchesItem = new JMenuItem(new TurnoutAction());
		JMenuItem routesItem = new JMenuItem(new RoutesAction());
		JMenuItem locomotivesItem = new JMenuItem(new LocomotivesAction());
		JMenuItem preferencesItem = new JMenuItem(new PreferencesAction());
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
		editMenu.add(switchesItem);
		editMenu.add(routesItem);
		editMenu.add(locomotivesItem);
		editMenu.add(new JSeparator());
		editMenu.add(preferencesItem);

		/* DAEMON */
		JMenu daemonMenu = new JMenu("Daemon");
		daemonConnectItem = new JMenuItem(new ConnectAction());
		daemonConnectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		daemonDisconnectItem = new JMenuItem(new DisconnectAction());
		daemonResetItem = new JMenuItem(new ResetAction());
		daemonDisconnectItem.setEnabled(false);
		daemonResetItem.setEnabled(false);
		daemonMenu.add(daemonConnectItem);
		daemonMenu.add(daemonDisconnectItem);
		daemonMenu.add(new JSeparator());
		daemonMenu.add(daemonResetItem);

		/* VIEW */
		JMenu viewMenu = new JMenu("View");
		JMenuItem refreshItem = new JMenuItem(new RefreshAction());
		JMenuItem fullscreenItem = new JMenuItem(new ToggleFullscreenAction());

		viewMenu.add(refreshItem);
		viewMenu.add(fullscreenItem);

		/* HELP */
		JMenu helpMenu = new JMenu("Help");

		addMenu(fileMenu);
		addMenu(editMenu);
		addMenu(daemonMenu);
		addMenu(viewMenu);
		addMenu(helpMenu);
		setJMenuBar(menuBar);
	}

	public void addMenu(JMenu menu) {
		menuBar.add(menu);
	}

	private void initToolbar() {
		/* FILE */
		JToolBar fileTooBar = new JToolBar();
		JButton exitToolBarButton = new SmallToolbarButton(new ExitAction());

		JButton openToolBarButton = new SmallToolbarButton(new OpenAction());
		JButton saveToolBarButton = new SmallToolbarButton(new SaveAction());
		fileTooBar.add(openToolBarButton);
		fileTooBar.add(saveToolBarButton);
		fileTooBar.add(exitToolBarButton);

		/* DIGITAL */
		JToolBar digitalToolBar = new JToolBar();
		JButton switchesToolBarButton = new SmallToolbarButton(
				new TurnoutAction());
		JButton routesToolBarButton = new SmallToolbarButton(new RoutesAction());
		JButton locomotivesToolBarButton = new SmallToolbarButton(
				new LocomotivesAction());
		JButton preferencesToolBarButton = new SmallToolbarButton(
				new PreferencesAction());

		digitalToolBar.add(switchesToolBarButton);
		digitalToolBar.add(routesToolBarButton);
		digitalToolBar.add(locomotivesToolBarButton);
		digitalToolBar.add(preferencesToolBarButton);

		/* DAEMON */
		JToolBar daemonToolBar = new JToolBar();
		hostnameLabel = new JLabel();
		hostnameLabel.setText(preferences.getStringValue("Hostname"));
		connectToolBarButton = new SmallToolbarButton(new ConnectAction());
		disconnectToolBarButton = new SmallToolbarButton(new DisconnectAction());
		disconnectToolBarButton.setEnabled(false);

		daemonToolBar.add(hostnameLabel);
		daemonToolBar.addSeparator();
		daemonToolBar.add(connectToolBarButton);
		daemonToolBar.add(disconnectToolBarButton);

		/* VIEWS */
		JToolBar viewToolBar = new JToolBar();
		JButton refreshButton = new SmallToolbarButton(new RefreshAction());
		toggleFullscreenButton = new SmallToolbarButton(
				new ToggleFullscreenAction());

		viewToolBar.add(refreshButton);
		viewToolBar.add(toggleFullscreenButton);

		/* ERROR */
		ErrorPanel errorPanel = new ErrorPanel();
		ExceptionProcessor.getInstance(errorPanel);

		toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		addToolBar(fileTooBar);
		addToolBar(digitalToolBar);
		addToolBar(daemonToolBar);
		addToolBar(viewToolBar);
		// toolbarPanel.add(errorPanel);

		JPanel toolbarErrorPanel = new JPanel(new BorderLayout(10, 10));
		toolbarErrorPanel.add(toolbarPanel, BorderLayout.WEST);
		toolbarErrorPanel.add(errorPanel, BorderLayout.EAST);

		add(toolbarErrorPanel, BorderLayout.PAGE_START);
	}

	public void addToolBar(JToolBar toolbar) {
		toolbarPanel.add(toolbar);
	}

	private JPanel initStatusBar() {
		JPanel statusBarPanel = new JPanel();
		commandHistoryModel = new DefaultComboBoxModel();
		commandHistory = new JComboBox(commandHistoryModel);
		commandHistory.setEditable(false);
		commandHistory.setFocusable(false);
		statusBarPanel.setLayout(new BorderLayout());
		statusBarPanel.add(commandHistory, BorderLayout.SOUTH);
		return statusBarPanel;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
		} catch (Exception e) {
		}
		if (args.length == 1) {
			new AdHocRailway(args[0]);
		} else {
			new AdHocRailway();
		}
	}

	public LocomotiveControlface getLocomotiveControl() {
		return locomotiveControl;
	}

	public void setLocomotiveControl(LocomotiveControlface locomotiveControl) {
		this.locomotiveControl = locomotiveControl;
	}

	public TurnoutControlIface getTurnoutControl() {
		return turnoutControl;
	}

	public TurnoutPersistenceIface getTurnoutPersistence() {
		return turnoutPersistence;
	}

	public LocomotivePersistenceIface getLocomotivePersistence() {
		return locomotivePersistence;
	}

	public RouteControlIface getRouteControl() {
		return routeControl;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public SRCPSession getSession() {
		return session;
	}

	public RoutePersistenceIface getRoutePersistence() {
		return routePersistence;
	}
}
