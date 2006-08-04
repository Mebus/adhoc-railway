
package ch.fork.AdHocRailway.ui.locomotives;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.locking.LockChangeListener;
import ch.fork.AdHocRailway.domain.locking.LockControl;
import ch.fork.AdHocRailway.domain.locking.exception.LockingException;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveChangeListener;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.NoneLocomotive;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import ch.fork.AdHocRailway.ui.ExceptionDialog;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfig;

public class LocomotiveWidget extends JPanel implements
    LocomotiveChangeListener, LockChangeListener {
    private static final long serialVersionUID = 1L;

    private enum LocomotiveActionType {
        ACCELERATE, DECCELERATE, TOGGLE_DIRECTION
    };

    private JComboBox                   locomotiveComboBox;
    private JComboBox                   locomotiveGroupComboBox;
    private JLabel                      image;
    private JLabel                      desc;
    private JProgressBar                speedBar;
    private JButton                     increaseSpeed;
    private JButton                     decreaseSpeed;
    private JLabel                      currentSpeed;
    private JButton                     stopButton;
    private JButton                     directionButton;
    private LockToggleButton            lockButton;
    private Locomotive                  myLocomotive;
    private int                         accelerateKey, deccelerateKey,
        toggleDirectionKey;
    private FunctionToggleButton[]      functionToggleButtons;
    private Color                       defaultBackground;
    private Locomotive                  none;
    private LocomotiveGroup             allLocomotives;
    private LocomotiveSelectAction      locomotiveSelectAction;
    private LocomotiveGroupSelectAction groupSelectAction;

    public LocomotiveWidget(int accelerateKey, int deccelerateKey,
        int toggleDirectionKey) {
        super();
        this.accelerateKey = accelerateKey;
        this.deccelerateKey = deccelerateKey;
        this.toggleDirectionKey = toggleDirectionKey;
        initGUI();
        initKeyboardActions();
    }

    private void initGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(200, 250));
        JPanel selectionPanel = initSelectionPanel();
        JPanel controlPanel = initControlPanel();
        JPanel centerPanel = new JPanel(new BorderLayout());
        desc = new JLabel(myLocomotive.getDesc(), SwingConstants.CENTER);

        addMouseListener(new MouseAction());
        centerPanel.add(controlPanel, BorderLayout.CENTER);
        centerPanel.add(desc, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(selectionPanel, BorderLayout.NORTH);
    }

    private JPanel initSelectionPanel() {
        JPanel selectionPanel = new JPanel(new BorderLayout(5, 5));
        locomotiveGroupComboBox = new JComboBox();
        locomotiveGroupComboBox.setFocusable(false);
        groupSelectAction = new LocomotiveGroupSelectAction();
        locomotiveComboBox = new JComboBox();
        none = new NoneLocomotive();
        locomotiveComboBox.addItem(none);
        locomotiveComboBox.setFocusable(false);
        myLocomotive = none;
        defaultBackground = locomotiveComboBox.getBackground();
        locomotiveSelectAction = new LocomotiveSelectAction();
        selectionPanel.add(locomotiveGroupComboBox, BorderLayout.NORTH);
        selectionPanel.add(locomotiveComboBox, BorderLayout.SOUTH);
        return selectionPanel;
    }

    private JPanel initControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        speedBar = new JProgressBar(JProgressBar.VERTICAL);
        speedBar.setPreferredSize(new Dimension(20, 200));
        controlPanel.add(speedBar, BorderLayout.EAST);
        JPanel functionsPanel = initFunctionsControl();
        JPanel speedControlPanel = initSpeedControl();
        controlPanel.add(functionsPanel, BorderLayout.WEST);
        controlPanel.add(speedControlPanel, BorderLayout.CENTER);
        return controlPanel;
    }

    private JPanel initFunctionsControl() {
        JPanel functionsPanel = new JPanel();
        BoxLayout bl = new BoxLayout(functionsPanel, BoxLayout.PAGE_AXIS);
        functionsPanel.setLayout(bl);
        Dimension size = new Dimension(60, 30);
        Insets margin = new Insets(2, 2, 2, 2);

        FunctionToggleButton functionButton = new FunctionToggleButton("Fn");
        FunctionToggleButton f1Button = new FunctionToggleButton("F1");
        FunctionToggleButton f2Button = new FunctionToggleButton("F2");
        FunctionToggleButton f3Button = new FunctionToggleButton("F3");
        FunctionToggleButton f4Button = new FunctionToggleButton("F4");
        functionToggleButtons = new FunctionToggleButton[] { functionButton,
            f1Button, f2Button, f3Button, f4Button };

        for (int i = 0; i < functionToggleButtons.length; i++) {
            functionToggleButtons[i].setMargin(margin);
            functionToggleButtons[i].setMaximumSize(size);
            functionToggleButtons[i]
                .addActionListener(new LocomotiveFunctionAction(i));
            functionsPanel.add(functionToggleButtons[i]);
        }
        return functionsPanel;
    }

    private JPanel initSpeedControl() {

        JPanel speedControlPanel = new JPanel();
        BoxLayout bl = new BoxLayout(speedControlPanel, BoxLayout.PAGE_AXIS);
        speedControlPanel.setLayout(bl);
        Dimension size = new Dimension(60, 30);
        Insets margin = new Insets(2, 2, 2, 2);

        increaseSpeed = new JButton("+");
        decreaseSpeed = new JButton("-");
        stopButton = new JButton("Stop");
        directionButton = new JButton(ImageTools.createImageIcon(
            "icons/forward.png", "Toggle Direction", this));
        lockButton = new LockToggleButton("");

        increaseSpeed.setMaximumSize(size);
        decreaseSpeed.setMaximumSize(size);
        stopButton.setMaximumSize(size);
        directionButton.setMaximumSize(size);
        lockButton.setMaximumSize(size);

        increaseSpeed.setMargin(margin);
        decreaseSpeed.setMargin(margin);
        stopButton.setMargin(margin);
        directionButton.setMargin(margin);
        lockButton.setMargin(margin);

        increaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        decreaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        directionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        increaseSpeed.addActionListener(new IncreaseSpeedAction());
        decreaseSpeed.addActionListener(new DecreaseSpeedAction());
        stopButton.addActionListener(new StopAction());
        directionButton.addActionListener(new ToggleDirectionAction());
        lockButton.addActionListener(new LockAction());

        speedControlPanel.add(increaseSpeed);
        speedControlPanel.add(decreaseSpeed);
        speedControlPanel.add(stopButton);
        speedControlPanel.add(directionButton);
        speedControlPanel.add(lockButton);
        return speedControlPanel;
    }

    private void initKeyboardActions() {
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(accelerateKey, 0), "acc" + accelerateKey);
        this.getActionMap().put("acc" + accelerateKey,
            new LocomotiveControlAction(LocomotiveActionType.ACCELERATE, 1));
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(deccelerateKey, 0), "dec" + deccelerateKey);
        this.getActionMap().put("dec" + deccelerateKey,
            new LocomotiveControlAction(LocomotiveActionType.DECCELERATE, 1));
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(toggleDirectionKey, 0),
            "tog" + toggleDirectionKey);
        this.getActionMap().put(
            "tog" + toggleDirectionKey,
            new LocomotiveControlAction(LocomotiveActionType.TOGGLE_DIRECTION,
                1));
    }

    protected void updateWidget() {
        double speedInPercent = ((double) myLocomotive.getCurrentSpeed())
            / ((double) myLocomotive.getDrivingSteps());
        if (speedInPercent > 0.9) {
            speedBar.setForeground(new Color(255, 0, 0));
        } else if (speedInPercent > 0.7) {
            speedBar.setForeground(new Color(255, 255, 0));
        } else {
            speedBar.setForeground(new Color(0, 255, 0));
        }
        speedBar.setMinimum(0);
        speedBar.setMaximum(myLocomotive.getDrivingSteps());
        speedBar.setValue(myLocomotive.getCurrentSpeed());
        boolean functions[] = myLocomotive.getFunctions();
        for (int i = 0; i < functions.length; i++) {
            functionToggleButtons[i].setSelected(functions[i]);
        }
        switch (myLocomotive.getDirection()) {
        case FORWARD:
            directionButton.setIcon(ImageTools.createImageIcon(
                "icons/forward.png", "Forward", this));
            break;
        case REVERSE:
            directionButton.setIcon(ImageTools.createImageIcon(
                "icons/back.png", "Reverse", this));
            break;
        default:
            directionButton.setIcon(ImageTools.createImageIcon(
                "icons/forward.png", "Forward", this));
        }
        lockButton.setSelected(myLocomotive.isLocked());
        if (myLocomotive.isLocked()) {
            
            if (LockControl.getInstance().getSessionID() == myLocomotive
                .getLockedBySession()) {
                lockButton.setSelectedIcon(ImageTools.createImageIcon(
                    "icons/locked_by_me.png", "Locked by me", this));
            } else {
                lockButton.setSelectedIcon(ImageTools.createImageIcon(
                    "icons/locked_by_enemy.png", "Locked by enemy", this));
            }            
        }
        lockButton.revalidate();
        lockButton.repaint();
        setPreferredSize(new Dimension(200, 250));
    }

    public void updateLocomotiveGroups(
        Collection<LocomotiveGroup> locomotiveGroups) {
        locomotiveComboBox.removeActionListener(locomotiveSelectAction);
        locomotiveGroupComboBox.removeActionListener(groupSelectAction);
        allLocomotives = new LocomotiveGroup("All");
        for (LocomotiveGroup lg : locomotiveGroups) {
            locomotiveGroupComboBox.addItem(lg);
            for (Locomotive l : lg.getLocomotives()) {
                locomotiveComboBox.addItem(l);
            }
        }
        locomotiveGroupComboBox.insertItemAt(allLocomotives, 0);
        locomotiveGroupComboBox.setSelectedItem(allLocomotives);
        locomotiveComboBox.addActionListener(locomotiveSelectAction);
        locomotiveGroupComboBox.addActionListener(groupSelectAction);
    }

    public Locomotive getMyLocomotive() {
        return myLocomotive;
    }

    public void locomotiveChanged(Locomotive changedLocomotive) {
        if (myLocomotive.equals(changedLocomotive)) {
            SwingUtilities.invokeLater(new LocomotiveWidgetUpdater(
                changedLocomotive));
        }
    }

    public void lockChanged(ControlObject changedLock) {
        if (changedLock instanceof Locomotive) {
            Locomotive changedLocomotive = (Locomotive) changedLock;
            locomotiveChanged(changedLocomotive);
        }
    }

    private class LocomotiveWidgetUpdater implements Runnable {
        private Locomotive locomotive;

        public LocomotiveWidgetUpdater(Locomotive l) {
            this.locomotive = l;
        }

        public void run() {
            updateWidget();
        }
    }
    private class LocomotiveFunctionAction extends AbstractAction {
        private int function;

        public LocomotiveFunctionAction(int function) {
            this.function = function;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                boolean[] functions = myLocomotive.getFunctions();
                functions[function] = functionToggleButtons[function]
                    .isSelected();
                LocomotiveControl.getInstance().setFunctions(myLocomotive,
                    functions);
            } catch (LocomotiveException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
            speedBar.requestFocus();
        }
    }
    private class LocomotiveControlAction extends AbstractAction {
        private LocomotiveActionType type;
        private long                 time = 0;
        private int                  locomotiveNumber;

        public LocomotiveControlAction(LocomotiveActionType type,
            int locomotiveNumber) {
            this.type = type;
            this.locomotiveNumber = locomotiveNumber;
        }

        public void actionPerformed(ActionEvent e) {
            if (time == 0 || e.getWhen() > time + 200) {
                try {
                    if (type == LocomotiveActionType.ACCELERATE) {
                        LocomotiveControl.getInstance().increaseSpeed(
                            myLocomotive);
                    } else if (type == LocomotiveActionType.DECCELERATE) {
                        LocomotiveControl.getInstance().decreaseSpeed(
                            myLocomotive);
                    } else if (type == LocomotiveActionType.TOGGLE_DIRECTION) {
                        LocomotiveControl.getInstance().toggleDirection(
                            myLocomotive);
                    }
                    if (time == 0) {
                        time = System.currentTimeMillis();
                    } else {
                        time = 0;
                    }
                } catch (LocomotiveException e3) {
                    ExceptionProcessor.getInstance().processException(e3);
                }
            } else {
                if (e.getWhen() > time + 1000) {
                    try {
                        if (type == LocomotiveActionType.ACCELERATE) {
                            LocomotiveControl.getInstance().increaseSpeedStep(
                                myLocomotive);
                        } else if (type == LocomotiveActionType.DECCELERATE) {
                            LocomotiveControl.getInstance().decreaseSpeedStep(
                                myLocomotive);
                        }
                    } catch (LocomotiveException e3) {
                        ExceptionProcessor.getInstance().processException(e3);
                    }
                    time = 0;
                }
            }
            updateWidget();
        }
    }
    class LocomotiveSelectAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (locomotiveComboBox.getItemCount() == 0) {
                return;
            }
            if (myLocomotive.getCurrentSpeed() == 0) {
                locomotiveComboBox.setBackground(defaultBackground);
                myLocomotive = (Locomotive) locomotiveComboBox
                    .getSelectedItem();
                updateWidget();
                desc.setText(myLocomotive.getDesc());
                speedBar.requestFocus();
            } else {
                locomotiveComboBox.setSelectedItem(myLocomotive);
                locomotiveComboBox.setBackground(Color.RED);
            }
        }
    }
    class LocomotiveGroupSelectAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            LocomotiveGroup lg = (LocomotiveGroup) locomotiveGroupComboBox
                .getSelectedItem();
            locomotiveComboBox.removeAllItems();
            Locomotive none = new NoneLocomotive();
            locomotiveComboBox.addItem(none);
            if (lg == allLocomotives) {
                SortedSet<Locomotive> sl = new TreeSet<Locomotive>(
                    LocomotiveControl.getInstance().getLocomotives());
                for (Locomotive l : sl) {
                    locomotiveComboBox.addItem(l);
                }
            } else {
                for (Locomotive l : lg.getLocomotives()) {
                    locomotiveComboBox.addItem(l);
                }
            }
            locomotiveComboBox.setSelectedIndex(0);
            locomotiveComboBox.revalidate();
            locomotiveComboBox.repaint();
        }
    }
    class StopAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().setSpeed(myLocomotive, 0);
                updateWidget();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }
            speedBar.requestFocus();
        }
    }
    class ToggleDirectionAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().toggleDirection(myLocomotive);
            } catch (LocomotiveException e1) {
                ExceptionDialog.getInstance().processException(e1);
            }
            speedBar.requestFocus();
            updateWidget();
        }
    }
    class IncreaseSpeedAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().increaseSpeed(myLocomotive);
                updateWidget();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }
            speedBar.requestFocus();
        }
    }
    class DecreaseSpeedAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().decreaseSpeed(myLocomotive);
                updateWidget();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }
            speedBar.requestFocus();
        }
    }

    private class LockAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            LockControl lc = LockControl.getInstance();
            try {
                if (lockButton.isSelected()) {
                    boolean succeeded = lc.acquireLock(myLocomotive);
                    lockButton.setSelected(succeeded);
                } else {
                    boolean succeeded = !lc.releaseLock(myLocomotive);
                    lockButton.setSelected(succeeded);
                }
                updateWidget();
            } catch (LockingException e1) {
                lockButton.setSelected(false);
                ExceptionProcessor.getInstance().processException(e1);
            }
        }

    }

    private class MouseAction extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {

            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                LocomotiveConfig locomotiveConfig = new LocomotiveConfig(
                    myLocomotive);
                if (locomotiveConfig.isOkPressed()) {
                    LocomotiveControl lc = LocomotiveControl.getInstance();
                    lc.unregisterLocomotive(myLocomotive);

                    myLocomotive = locomotiveConfig.getLocomotive();
                    lc.registerLocomotive(myLocomotive);
                    desc.setText(myLocomotive.getDesc());
                    locomotiveChanged(myLocomotive);
                }
            }
        }
    }
}
