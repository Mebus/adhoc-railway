
package ch.fork.AdHocRailway.ui;

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import ch.fork.AdHocRailway.domain.configuration.Preferences;
import ch.fork.AdHocRailway.domain.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.domain.configuration.XMLExporter;
import ch.fork.AdHocRailway.domain.configuration.XMLImporter;
import ch.fork.AdHocRailway.domain.configuration.exception.ConfigurationException;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.locking.LockControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveControlPanel;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfigurationDialog;
import ch.fork.AdHocRailway.ui.switches.SwitchProgrammer;
import ch.fork.AdHocRailway.ui.switches.SwitchesControlPanel;
import ch.fork.AdHocRailway.ui.switches.configuration.SwitchConfigurationDialog;
import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

public class AdHocRailway extends JFrame implements CommandDataListener,
    InfoDataListener {
    private static final long      serialVersionUID = 1L;
    private static final String    NAME             = "AdHoc-Railway";
    private SRCPSession            session;
    private String                 typedChars       = "";
    // GUI-Components
    private LocomotiveControlPanel locomotiveControlPanel;
    private JPanel                 statusBarPanel;
    private JLabel                 hostnameLabel;
    private JButton                connectToolBarButton;
    private JButton                disconnectToolBarButton;
    private JComboBox              commandHistory;
    private DefaultComboBoxModel   commandHistoryModel;
    private JMenuItem              daemonConnectItem;
    private JMenuItem              daemonDisconnectItem;
    private JMenuItem              daemonResetItem;
    private JMenu                  recentFilesMenu;
    private File                   actualFile;
    private File                   logFile;
    private FileWriter             logFileWriter;
    private SwitchesControlPanel   switchesControlPanel;
    private JButton                toggleFullscreenButton;
    private JMenuBar               menuBar;


    public AdHocRailway() {
        this(null);
    }

    public AdHocRailway(String file) {
        super(NAME);

        SplashWindow splash = new SplashWindow(createImageIcon(
            "icons/splash.png", "RailControl", AdHocRailway.this), this, 500,
            4);
        splash.nextStep("Starting AdHoc-Railway ...");
        setIconImage(createImageIcon("icons/RailControl.png", "RailControl",
            AdHocRailway.this).getImage());

        initGUI();

        ExceptionDialog.start(this);
        if (file != null) {
            File standardFile = new File(file);
            if (standardFile.exists()) {
                splash.nextStep("Loading configuration from " + file + " ...");
                OpenAction oa = new OpenAction(null);
                oa.openFile(standardFile);
            }
        } else {
            splash.nextStep("");
        }
        splash.nextStep("Creating GUI ...");
        switchesControlPanel.update(SwitchControl.getInstance()
            .getSwitchGroups());
        locomotiveControlPanel.update(LocomotiveControl.getInstance()
            .getLocomotiveGroups());

        switchesControlPanel.revalidate();
        locomotiveControlPanel.revalidate();

        setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
        setVisible(true);

        splash.nextStep("RailControl started");
        updateCommandHistory("RailControl started");
    }

    private void initGUI() {
        setFont(new Font("Verdana", Font.PLAIN, 19));
        setLayout(new BorderLayout());
        ExceptionProcessor.getInstance(this);
        switchesControlPanel = initSwitchPanel();
        locomotiveControlPanel = initLocomotiveControl();
        initMenu();
        initToolbar();
        initStatusBar();
        JPanel switchesAndLocomotivesPanel = new JPanel(new BorderLayout(5, 5));
        switchesAndLocomotivesPanel.add(switchesControlPanel,
            BorderLayout.CENTER);
        switchesAndLocomotivesPanel.add(locomotiveControlPanel,
            BorderLayout.SOUTH);
        add(switchesAndLocomotivesPanel, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ExitAction a = new ExitAction();
                a.actionPerformed(null);
                super.windowClosing(e);
            }
        });
    }

    private SwitchesControlPanel initSwitchPanel() {
        SwitchesControlPanel switchesControlPanel = new SwitchesControlPanel(
            this);
        switchesControlPanel.setBorder(new EtchedBorder());
        return switchesControlPanel;
    }

    private LocomotiveControlPanel initLocomotiveControl() {
        LocomotiveControlPanel locomotiveControlPanel = new LocomotiveControlPanel(
            this);
        locomotiveControlPanel.setBorder(new EtchedBorder());
        return locomotiveControlPanel;
    }

    public static void main(String[] args) {

        if (args.length == 1) {
            new AdHocRailway(args[0]);
        } else {
            new AdHocRailway();
        }
    }

    public void commandDataSent(String commandData) {
        updateCommandHistory("To Server: " + commandData);
    }

    public void infoDataReceived(String infoData) {
        updateCommandHistory("From Server: " + infoData);
    }

    public void updateCommandHistory(String text) {
        if (Preferences.getInstance().getBooleanValue(PreferencesKeys.LOGGING)) {
            DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
            String date = df.format(GregorianCalendar.getInstance().getTime());
            String fullText = "[" + date + "]: " + text;
            SwingUtilities.invokeLater(new CommandHistoryUpdater(fullText));
        }
    }

    private class CommandHistoryUpdater implements Runnable {
        private String text;

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
        private File file;

        public OpenAction(File file) {
            super("Open file...", createImageIcon("icons/fileopen.png",
                "File open...", AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            if (file == null) {
                JFileChooser fileChooser = new JFileChooser(new File("."));
                int returnVal = fileChooser.showOpenDialog(AdHocRailway.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    openFile(file);
                } else {
                    updateCommandHistory("Open command cancelled by user");
                }
            } else {
                // openFile(file);
            }
        }

        private void openFile(File file) {
            LocomotiveControl lc = LocomotiveControl.getInstance();
            SwitchControl sc = SwitchControl.getInstance();

            try {
                XMLImporter importer = new XMLImporter(file.getAbsolutePath());

                hostnameLabel.setText(Preferences.getInstance().getStringValue(
                    PreferencesKeys.HOSTNAME));
                if (recentFilesMenu.getComponentCount() > 1) {
                    recentFilesMenu.remove(10);
                }
                JMenuItem recentItem = new JMenuItem(file.getPath());
                recentItem.addActionListener(new OpenAction(file));
                recentFilesMenu.add(recentItem, 0);
                recentFilesMenu.repaint();
                recentFilesMenu.revalidate();
                actualFile = file;
                updateCommandHistory("Opened configuration: " + file.getName());
                setTitle(AdHocRailway.NAME + " : [ "
                    + actualFile.getAbsolutePath() + " ]");
            } catch (ConfigurationException e) {
                ExceptionProcessor.getInstance().processException(
                    "Error loading file", e);
            }

        }
    }
    private class SaveAsAction extends AbstractAction {
        public SaveAsAction() {
            super("Save as...", createImageIcon("icons/filesaveas.png",
                "Save as...", AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(new File("."));
            int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // This is where a real application would open
                // the file.
                FileWriter fileWriter = null;
                try {
                    String xmlConfig = XMLExporter.export(0.2);
                    fileWriter = new FileWriter(file);
                    fileWriter.write(xmlConfig);
                    updateCommandHistory("Configuration saved: "
                        + file.getName());
                } catch (IOException e1) {
                    ExceptionProcessor.getInstance().processException(
                        "Error writing file", e1);
                } catch (ConfigurationException e1) {
                    ExceptionProcessor.getInstance().processException(
                        "Error writing file", e1);
                } finally {
                    try {
                        fileWriter.close();
                    } catch (IOException e1) {
                        ExceptionProcessor.getInstance().processException(
                            "Error writing file", e1);
                    }
                }
            } else {
                updateCommandHistory("Save cancelled by user");
            }
        }
    }
    private class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save", createImageIcon("icons/filesave.png", "Save",
                AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            if (actualFile != null) {

                FileWriter fileWriter = null;
                try {
                    String xmlConfig = XMLExporter.export(0.2);
                    fileWriter = new FileWriter(actualFile);
                    fileWriter.write(xmlConfig);
                    updateCommandHistory("Configuration saved: "
                        + actualFile.getName());
                } catch (ConfigurationException e1) {
                    ExceptionProcessor.getInstance().processException(
                        "Error writing file", e1);

                } catch (IOException e1) {
                    ExceptionProcessor.getInstance().processException(
                        "Error writing file", e1);
                } finally {
                    try {
                        fileWriter.close();
                    } catch (IOException e1) {
                        ExceptionProcessor.getInstance().processException(
                            "Error writing file", e1);
                    }
                }
            } else {
                SaveAsAction sa = new SaveAsAction();
                sa.actionPerformed(e);
            }
        }
    }
    private class ExitAction extends AbstractAction {
        public ExitAction() {
            super("Exit", createImageIcon("icons/exit.png", "Exit",
                AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
                "Really exit ?", "Exit", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, ImageTools.createImageIcon(
                    "icons/messagebox_warning.png", "Warning", this));
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }
    private class SwitchesAction extends AbstractAction {
        public SwitchesAction() {
            super("Switches", createImageIcon("icons/switch.png", "Switches",
                AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            SwitchConfigurationDialog switchConfig = new SwitchConfigurationDialog(
                AdHocRailway.this);
            if (switchConfig.isOkPressed()) {
                SwitchControl sc = SwitchControl.getInstance();
                sc.unregisterAllSwitchGroups();
                sc.registerSwitchGroups(switchConfig.getSwitchGroups());
                sc.unregisterAllSwitches();
                sc.registerSwitches(switchConfig.getSwitchNumberToSwitch()
                    .values());
                switchesControlPanel.update(sc.getSwitchGroups());
                updateCommandHistory("Switch configuration changed");
            }
        }
    }
    private class LocomotivesAction extends AbstractAction {
        public LocomotivesAction() {
            super("Locomotives", createImageIcon("icons/locomotive.png",
                "Locomotives", AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            LocomotiveControl lc = LocomotiveControl.getInstance();
            LocomotiveConfigurationDialog locomotiveConfig = new LocomotiveConfigurationDialog(
                AdHocRailway.this);
            if (locomotiveConfig.isOkPressed()) {

                lc.unregisterAllLocomotives();
                lc.unregisterAllLocomotiveGroups();
                lc.registerLocomotives(locomotiveConfig.getLocomotives());
                lc.registerLocomotiveGroups(locomotiveConfig
                    .getLocomotiveGroups());
                locomotiveControlPanel.update(lc.getLocomotiveGroups());

                updateCommandHistory("Locomotive configuration changed");
            }
        }
    }
    private class PreferencesAction extends AbstractAction {
        public PreferencesAction() {
            super("Preferences", createImageIcon("icons/package_settings.png",
                "Preferences", AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            PreferencesDialog p = new PreferencesDialog(AdHocRailway.this);
            p.editPreferences(Preferences.getInstance());
            if (p.isOkPressed()) {
                locomotiveControlPanel.update(LocomotiveControl.getInstance()
                    .getLocomotiveGroups());
                switchesControlPanel.update(SwitchControl.getInstance()
                    .getSwitchGroups());
                hostnameLabel.setText(Preferences.getInstance().getStringValue(
                    "Hostname"));
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
            super("Connect", createImageIcon("icons/daemonconnect.png",
                "Connect", AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String host = Preferences.getInstance().getStringValue(
                    PreferencesKeys.HOSTNAME);
                int port = Preferences.getInstance().getIntValue(
                    PreferencesKeys.PORT);
                session = new SRCPSession(host, port, false);
                session.getCommandChannel().addCommandDataListener(
                    AdHocRailway.this);
                session.getInfoChannel().addInfoDataListener(AdHocRailway.this);
                SwitchControl.getInstance().setSession(session);
                LocomotiveControl.getInstance().setSession(session);
                LockControl.getInstance().setSession(session);
                session.connect();
                daemonConnectItem.setEnabled(false);
                daemonDisconnectItem.setEnabled(true);
                daemonResetItem.setEnabled(true);
                connectToolBarButton.setEnabled(false);
                disconnectToolBarButton.setEnabled(true);
                updateCommandHistory("Connected to server " + host
                    + " on port " + port);
                // debugDialog = new DebugDialog(session, AdHocRailway.this);
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
            super("Disconnect", createImageIcon("icons/daemondisconnect.png",
                "Disconnect", AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String host = Preferences.getInstance().getStringValue(
                    PreferencesKeys.HOSTNAME);
                int port = Preferences.getInstance().getIntValue(
                    PreferencesKeys.PORT);
                session.disconnect();
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
            super("Reset", createImageIcon("icons/daemonreset.png", "Reset",
                AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
        }
    }
    private class RefreshAction extends AbstractAction {
        public RefreshAction() {
            super("Refresh", createImageIcon("icons/reload.png", "Refresh",
                AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            switchesControlPanel.update(SwitchControl.getInstance()
                .getSwitchGroups());
            locomotiveControlPanel.update(LocomotiveControl.getInstance()
                .getLocomotiveGroups());
        }
    }
    private class SwitchProgrammerAction extends AbstractAction {
        public SwitchProgrammerAction() {
            super("SwitchProgrammer", createImageIcon(
                "icons/switch_programmer.png", "SwitchProgrammer",
                AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            SwitchProgrammer sp = new SwitchProgrammer(AdHocRailway.this,
                session);
        }
    }
    private class ToggleFullscreenAction extends AbstractAction {

        boolean fullscreen = false;

        public ToggleFullscreenAction() {
            super("ToggleFullscreen", createImageIcon(
                "icons/window_nofullscreen.png", "ToggleFullscreen",
                AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            if (fullscreen) {
                dispose();
                menuBar.setVisible(true);
                setResizable(true);
                setUndecorated(false);
                setSize(1000, 700);
                setVisible(true);
                toggleFullscreenButton.setIcon(createImageIcon(
                    "icons/window_fullscreen.png", "ToggleFullscreen",
                    AdHocRailway.this));
                fullscreen = false;
            } else {
                dispose();
                menuBar.setVisible(false);
                setResizable(false);
                setUndecorated(true);
                setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
                setVisible(true);
                toggleFullscreenButton.setIcon(createImageIcon(
                    "icons/window_nofullscreen.png", "ToggleFullscreen",
                    AdHocRailway.this));
                fullscreen = true;
            }
        }
    }
    private class SwitchesStraightAction extends AbstractAction {
        public SwitchesStraightAction() {
            super("", createImageIcon("icons/switch.png",
                "Set all switches straight", AdHocRailway.this));
        }

        public void actionPerformed(ActionEvent e) {
            SwitchControl sc = SwitchControl.getInstance();

            for (Switch s : sc.getNumberToSwitch().values()) {
                try {
                    sc.setStraight(s);
                    Thread.sleep(10);
                } catch (ControlException e1) {
                    ExceptionProcessor.getInstance().processException(e1);
                    return;
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }

        }
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        /* FILE */
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem openItem = new JMenuItem(new OpenAction(null));
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
            ActionEvent.CTRL_MASK));
        JMenuItem saveAsItem = new JMenuItem(new SaveAsAction());
        JMenuItem saveItem = new JMenuItem(new SaveAction());
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
            ActionEvent.CTRL_MASK));
        recentFilesMenu = new JMenu("Recent files...");
        JMenuItem exitItem = new JMenuItem(new ExitAction());
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
            ActionEvent.CTRL_MASK));
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(recentFilesMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitItem);
        /* EDIT */
        JMenu edit = new JMenu("Edit");
        JMenuItem switchesItem = new JMenuItem(new SwitchesAction());
        JMenuItem locomotivesItem = new JMenuItem(new LocomotivesAction());
        JMenuItem preferencesItem = new JMenuItem(new PreferencesAction());
        edit.setMnemonic(KeyEvent.VK_E);
        switchesItem.setMnemonic(KeyEvent.VK_S);
        switchesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
            ActionEvent.ALT_MASK));
        locomotivesItem.setMnemonic(KeyEvent.VK_L);
        locomotivesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
            ActionEvent.ALT_MASK));
        preferencesItem.setMnemonic(KeyEvent.VK_P);
        preferencesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
            ActionEvent.ALT_MASK));
        edit.add(switchesItem);
        edit.add(locomotivesItem);
        edit.add(new JSeparator());
        edit.add(preferencesItem);
        /* DAEMON */
        JMenu daemonMenu = new JMenu("Daemon");
        daemonConnectItem = new JMenuItem(new ConnectAction());
        daemonDisconnectItem = new JMenuItem(new DisconnectAction());
        daemonResetItem = new JMenuItem(new ResetAction());
        daemonDisconnectItem.setEnabled(false);
        daemonResetItem.setEnabled(false);
        daemonMenu.add(daemonConnectItem);
        daemonMenu.add(daemonDisconnectItem);
        daemonMenu.add(new JSeparator());
        daemonMenu.add(daemonResetItem);
        /* HELP */
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(edit);
        menuBar.add(daemonMenu);
        menuBar.add(Box.createGlue());
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void initToolbar() {
        JToolBar toolBar = new JToolBar();
        JButton openToolBarButton = new JButton(new OpenAction(null));
        openToolBarButton.setFocusable(false);
        JButton saveToolBarButton = new JButton(new SaveAction());
        saveToolBarButton.setFocusable(false);
        JButton saveAsToolBarButton = new JButton(new SaveAsAction());
        saveAsToolBarButton.setFocusable(false);
        JButton exitToolBarButton = new JButton(new ExitAction());
        exitToolBarButton.setFocusable(false);
        JButton switchesToolBarButton = new JButton(new SwitchesAction());
        switchesToolBarButton.setFocusable(false);
        JButton locomotivesToolBarButton = new JButton(new LocomotivesAction());
        locomotivesToolBarButton.setFocusable(false);
        JButton preferencesToolBarButton = new JButton(new PreferencesAction());
        preferencesToolBarButton.setFocusable(false);
        hostnameLabel = new JLabel();
        hostnameLabel.setText(Preferences.getInstance().getStringValue(
            "Hostname"));
        connectToolBarButton = new JButton(new ConnectAction());
        connectToolBarButton.setFocusable(false);
        disconnectToolBarButton = new JButton(new DisconnectAction());
        disconnectToolBarButton.setFocusable(false);
        disconnectToolBarButton.setEnabled(false);
        JButton setAllSwitchesStraightButton = new JButton(
            new SwitchesStraightAction());
        setAllSwitchesStraightButton.setFocusable(false);
        JButton refreshButton = new JButton(new RefreshAction());
        refreshButton.setFocusable(false);
        JButton switchProgrammerButton = new JButton(
            new SwitchProgrammerAction());
        switchProgrammerButton.setFocusable(false);

        toggleFullscreenButton = new JButton(new ToggleFullscreenAction());
        toggleFullscreenButton.setFocusable(false);

        openToolBarButton.setText("");
        saveToolBarButton.setText("");
        saveAsToolBarButton.setText("");
        exitToolBarButton.setText("");
        switchesToolBarButton.setText("");
        locomotivesToolBarButton.setText("");
        preferencesToolBarButton.setText("");
        connectToolBarButton.setText("");
        disconnectToolBarButton.setText("");
        refreshButton.setText("");
        switchProgrammerButton.setText("");
        toggleFullscreenButton.setText("");

        toolBar.add(openToolBarButton);
        toolBar.add(saveToolBarButton);
        toolBar.add(exitToolBarButton);
        toolBar.addSeparator();
        toolBar.add(switchesToolBarButton);
        toolBar.add(locomotivesToolBarButton);
        toolBar.add(preferencesToolBarButton);
        toolBar.addSeparator();
        toolBar.add(hostnameLabel);
        toolBar.addSeparator();
        toolBar.add(connectToolBarButton);
        toolBar.add(disconnectToolBarButton);
        toolBar.addSeparator();
        toolBar.add(setAllSwitchesStraightButton);
        toolBar.add(refreshButton);
        toolBar.add(switchProgrammerButton);
        toolBar.add(toggleFullscreenButton);

        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.add(toolBar, BorderLayout.WEST);
        add(toolbarPanel, BorderLayout.PAGE_START);
    }

    private void initStatusBar() {
        statusBarPanel = new JPanel();
        commandHistoryModel = new DefaultComboBoxModel();
        commandHistory = new JComboBox(commandHistoryModel);
        commandHistory.setEditable(false);
        commandHistory.setFocusable(false);
        statusBarPanel.setLayout(new BorderLayout());
        statusBarPanel.add(commandHistory, BorderLayout.SOUTH);
    }

    public void infoDataReceived(double timestamp, int bus, String deviceGroup,
        String data) {

    }
}
