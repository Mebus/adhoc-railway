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

package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.LocomotiveChangeListener;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.LocomotiveManagerListener;
import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.bus.events.EndImportEvent;
import ch.fork.AdHocRailway.ui.bus.events.StartImportEvent;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfig;
import ch.fork.AdHocRailway.ui.tools.ImageTools;
import ch.fork.AdHocRailway.utils.LocomotiveHelper;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import static ch.fork.AdHocRailway.ui.tools.ImageTools.createImageIcon;
import static ch.fork.AdHocRailway.ui.tools.ImageTools.createImageIconFromIconSet;

public class LocomotiveWidget extends JPanel implements
        LocomotiveChangeListener, LocomotiveManagerListener {


    private final int number;
    private final List<FunctionToggleButton> functionToggleButtons = new ArrayList<FunctionToggleButton>();
    private final JFrame frame;
    private final LocomotiveGroup allLocomotivesGroup;
    private final LocomotiveContext ctx;
    public boolean directionToggeled;
    private JComboBox<Locomotive> locomotiveComboBox;
    private JComboBox<LocomotiveGroup> locomotiveGroupComboBox;
    private JProgressBar speedBar;
    private JButton increaseSpeed;
    private JButton decreaseSpeed;
    private JButton stopButton;
    private JButton directionButton;
    private LockToggleButton lockButton;
    private Locomotive myLocomotive;
    private LocomotiveSelectAction locomotiveSelectAction;
    private LocomotiveGroupSelectAction groupSelectAction;
    private JPanel functionsPanel;
    private DefaultComboBoxModel<LocomotiveGroup> locomotiveGroupComboBoxModel;
    private DefaultComboBoxModel<Locomotive> locomotiveComboBoxModel;

    private boolean ignoreGroupAndLocomotiveSelectionEvents;
    private boolean disableListener;
    private boolean connectedToRailway;

    public LocomotiveWidget(final LocomotiveContext ctx, final int number,
                            final JFrame frame) {
        super();
        this.ctx = ctx;
        this.number = number;
        this.frame = frame;

        ctx.getLocomotiveControl().addLocomotiveChangeListener(myLocomotive,
                this);

        ctx.getMainBus().register(this);
        initGUI();
        initKeyboardActions();

        allLocomotivesGroup = new LocomotiveGroup("", "All");
        ctx.getLocomotiveManager().addLocomotiveManagerListener(this);
        connectedToRailway = false;
    }

    @Subscribe
    public void connectedToRailwayDevice(final ConnectedToRailwayEvent event) {
        if (event.isConnected()) {
            if (myLocomotive != null) {
                ctx.getLocomotiveControl().addLocomotiveChangeListener(
                        myLocomotive, this);
            }
            connectedToRailway = true;
        } else {
            connectedToRailway = false;
            ctx.getLocomotiveControl().removeLocomotiveChangeListener(this);
        }
    }

    @Subscribe
    public void startImport(final StartImportEvent event) {
        disableListener = true;
    }

    @Subscribe
    public void endImport(final EndImportEvent event) {
        disableListener = false;
        updateLocomotiveGroups(ctx.getLocomotiveManager().getAllLocomotiveGroups());
    }

    public void updateLocomotiveGroups(final SortedSet<LocomotiveGroup> groups) {
        if (myLocomotive != null) {
            return;
        }

        ignoreGroupAndLocomotiveSelectionEvents = true;
        locomotiveGroupComboBoxModel.removeAllElements();
        locomotiveComboBoxModel.removeAllElements();
        allLocomotivesGroup.getLocomotives().clear();

        locomotiveGroupComboBoxModel.addElement(allLocomotivesGroup);

        for (final LocomotiveGroup lg : groups) {
            for (final Locomotive l : lg.getLocomotives()) {
                allLocomotivesGroup.addLocomotive(l);
                locomotiveComboBoxModel.addElement(l);
            }
            locomotiveGroupComboBoxModel.addElement(lg);
        }

        locomotiveComboBox.setSelectedIndex(-1);
        ignoreGroupAndLocomotiveSelectionEvents = false;
        connectedToRailway = false;
    }

    public Locomotive getMyLocomotive() {
        return myLocomotive;
    }

    private void initGUI() {
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        setLayout(new MigLayout("wrap 3"));

        initSelectionPanel();
        final JPanel controlPanel = initControlPanel();

        addMouseListener(new MouseAction());

        add(controlPanel, "span 3, grow");

        addMouseWheelListener(new WheelControl());

    }

    private void initSelectionPanel() {
        locomotiveGroupComboBox = new JComboBox<LocomotiveGroup>();
        locomotiveGroupComboBoxModel = new DefaultComboBoxModel<LocomotiveGroup>();
        locomotiveGroupComboBox.setModel(locomotiveGroupComboBoxModel);
        locomotiveGroupComboBox.setFocusable(false);
        locomotiveGroupComboBox.setFont(locomotiveGroupComboBox.getFont()
                .deriveFont(14));
        locomotiveGroupComboBox.setMaximumRowCount(10);
        locomotiveGroupComboBox.setSelectedIndex(-1);

        groupSelectAction = new LocomotiveGroupSelectAction();
        locomotiveGroupComboBox.addItemListener(groupSelectAction);

        locomotiveComboBox = new JComboBox<Locomotive>();
        locomotiveComboBoxModel = new DefaultComboBoxModel<Locomotive>();
        locomotiveComboBox.setModel(locomotiveComboBoxModel);
        locomotiveComboBox.setFocusable(false);
        locomotiveSelectAction = new LocomotiveSelectAction();
        locomotiveComboBox.addItemListener(locomotiveSelectAction);
        locomotiveComboBox.setRenderer(new LocomotiveComboBoxRenderer());

        add(locomotiveGroupComboBox, "span 3, grow, width 200");
        add(locomotiveComboBox, "span 3, grow, width 200");

    }

    private JPanel initControlPanel() {
        final JPanel controlPanel = new JPanel(new MigLayout("fill"));

        speedBar = new JProgressBar(SwingConstants.VERTICAL);

        speedBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                processMouseMovement(e);
            }
        });

        speedBar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseMoved(e);
                processMouseMovement(e);
            }
        });

        final JPanel functionsPanel = initFunctionsControl();
        final JPanel speedControlPanel = initSpeedControl();

        controlPanel.add(functionsPanel, "grow, west");
        controlPanel.add(speedControlPanel, "grow");
        controlPanel.add(speedBar, "east, width 45");

        return controlPanel;
    }

    private JPanel initFunctionsControl() {
        functionsPanel = new JPanel();

        return functionsPanel;
    }

    private JPanel initSpeedControl() {

        final JPanel speedControlPanel = new JPanel();
        speedControlPanel.setLayout(new MigLayout("wrap 1, fill"));

        increaseSpeed = new JButton("+");
        decreaseSpeed = new JButton("-");
        stopButton = new JButton("Stop");
        directionButton = new JButton(
                createImageIcon("crystal/forward.png"));
        lockButton = new LockToggleButton("");

        increaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        decreaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        directionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        increaseSpeed.addActionListener(new LocomotiveAccelerateAction());
        decreaseSpeed.addActionListener(new LocomotiveDeccelerateAction());
        stopButton.addActionListener(new StopAction());
        directionButton.addActionListener(new ToggleDirectionAction());
        lockButton.addActionListener(new LockAction());

        increaseSpeed.setFocusable(false);
        decreaseSpeed.setFocusable(false);
        stopButton.setFocusable(false);
        directionButton.setFocusable(false);
        lockButton.setFocusable(false);

        speedControlPanel.add(increaseSpeed, "height 30, growx");
        speedControlPanel.add(decreaseSpeed, "height 30, growx");
        speedControlPanel.add(stopButton, "height 30, growx");
        speedControlPanel.add(directionButton, "height 30, growx");
        speedControlPanel.add(lockButton, "height 30, growx");
        return speedControlPanel;
    }

    private void initKeyboardActions() {
        final KeyBoardLayout kbl = Preferences.getInstance()
                .getKeyBoardLayout();
        final InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        getActionMap().put("Accelerate" + number,
                new LocomotiveAccelerateAction());
        kbl.assignKeys(inputMap, "Accelerate" + number);
        getActionMap().put("Deccelerate" + number,
                new LocomotiveDeccelerateAction());
        kbl.assignKeys(inputMap, "Deccelerate" + number);
        getActionMap().put("ToggleDirection" + number,
                new LocomotiveToggleDirectionAction());
        kbl.assignKeys(inputMap, "ToggleDirection" + number);
    }

    private void updateFunctionButtons() {
        functionToggleButtons.clear();
        functionsPanel.removeAll();
        if (myLocomotive.getFunctions().size() > 5) {
            functionsPanel.setLayout(new MigLayout("wrap 2, fill"));
        } else {
            functionsPanel.setLayout(new MigLayout("wrap, fill"));
        }

        for (final LocomotiveFunction fn : myLocomotive.getFunctions()) {
            final FunctionToggleButton functionButton = new FunctionToggleButton(
                    fn.getShortDescription());
            functionToggleButtons.add(functionButton);
            final int i = functionToggleButtons.indexOf(functionButton);
            functionButton.addActionListener(new LocomotiveFunctionAction(i));
            functionButton.setToolTipText(fn.getDescription());

            functionButton.setFocusable(false);
            functionsPanel.add(functionButton, "height 30, width 60");
        }
        revalidate();
    }

    private void updateWidget() {
        if (myLocomotive == null) {
            return;
        }
        final LocomotiveController locomotiveControl = ctx
                .getLocomotiveControl();

        if (ctx.getRailwayDeviceManager().isConnected()) {
            locomotiveControl.activateLoco(myLocomotive);
        }
        updateSpeed();

        updateFunctions();

        updateDirection();

        updateLockedState(locomotiveControl);

        if (isFree()) {
            locomotiveGroupComboBox.setEnabled(true);
            locomotiveComboBox.setEnabled(true);
        } else {
            UIManager.put("ComboBox.disabledForeground", new ColorUIResource(
                    Color.BLACK));
            locomotiveGroupComboBox.setEnabled(false);
            locomotiveComboBox.setEnabled(false);
        }
        speedBar.requestFocus();
    }

    private void updateLockedState(final LocomotiveController locomotiveControl) {
        final boolean locked = locomotiveControl.isLocked(myLocomotive);
        lockButton.setSelected(locked);
        if (locked) {
            if (locomotiveControl.isLockedByMe(myLocomotive)) {
                lockButton.setSelectedIcon(ImageTools
                        .createImageIconFromCustom("locked_by_me.png"));

            } else {
                lockButton.setSelectedIcon(ImageTools
                        .createImageIconFromCustom("locked_by_enemy.png"));
            }
        }
    }

    private void updateSpeed() {
        final int currentSpeed = myLocomotive.getCurrentSpeed();
        final float speedInPercent = ((float) currentSpeed)
                / ((float) myLocomotive.getType().getDrivingSteps());

        final float hue = (1.0f - speedInPercent) * 0.3f;
        final Color speedColor = Color.getHSBColor(hue, 1.0f, 1.0f);

        speedBar.setForeground(speedColor);
        speedBar.setMinimum(0);
        speedBar.setMaximum(myLocomotive.getType().getDrivingSteps());
        speedBar.setValue(currentSpeed);
    }

    private void updateDirection() {
        switch (myLocomotive.getCurrentDirection()) {
            case FORWARD:
                directionButton.setIcon(createImageIconFromIconSet("forward.png"));
                break;
            case REVERSE:
                directionButton.setIcon(createImageIconFromIconSet("back.png"));
                break;
            default:
                directionButton.setIcon(createImageIconFromIconSet("forward.png"));
        }
    }

    private void updateFunctions() {
        final boolean[] functions = myLocomotive.getCurrentFunctions();
        for (int i = 0; i < functions.length; i++) {
            functionToggleButtons.get(i).setSelected(functions[i]);
        }

        if (myLocomotive.getType().getFunctionCount() == 0) {
            for (final FunctionToggleButton b : functionToggleButtons) {
                b.setEnabled(false);
            }
        } else {
            for (final FunctionToggleButton b : functionToggleButtons) {
                b.setEnabled(true);
            }
        }
    }

    private boolean isFree() {
        if (myLocomotive == null) {
            return true;
        }
        final LocomotiveController locomotiveControl = ctx
                .getLocomotiveControl();
        if (myLocomotive.getCurrentSpeed() == 0) {
            if (locomotiveControl.isLocked(myLocomotive)) {
                return !locomotiveControl.isLockedByMe(myLocomotive);
            } else {
                return true;
            }
        } else {
            if (locomotiveControl.isLocked(myLocomotive)) {
                return !locomotiveControl.isLockedByMe(myLocomotive);
            } else {
                return false;
            }
        }
    }

    @Override
    public void locomotiveChanged(final Locomotive changedLocomotive) {

        if (myLocomotive == null) {
            return;
        }

        if (myLocomotive.equals(changedLocomotive)) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateWidget();
                }
            });
        }

    }

    @Override
    public void locomotivesUpdated(
            final SortedSet<LocomotiveGroup> locomotiveGroups) {
        if (disableListener) {
            return;
        }
        updateLocomotiveGroups(locomotiveGroups);

    }

    @Override
    public void locomotiveAdded(final Locomotive locomotive) {
        if (disableListener) {
            return;
        }
        updateLocomotiveGroups(ctx.getLocomotiveManager().getAllLocomotiveGroups());

    }

    @Override
    public void locomotiveUpdated(final Locomotive locomotive) {
        if (disableListener) {
            return;
        }
        updateLocomotiveGroups(ctx.getLocomotiveManager().getAllLocomotiveGroups());

    }

    @Override
    public void locomotiveGroupAdded(final LocomotiveGroup group) {
        if (disableListener) {
            return;
        }
        updateLocomotiveGroups(ctx.getLocomotiveManager().getAllLocomotiveGroups());

    }

    @Override
    public void locomotiveRemoved(final Locomotive locomotive) {
        if (disableListener) {
            return;
        }
        updateLocomotiveGroups(ctx.getLocomotiveManager().getAllLocomotiveGroups());
    }

    @Override
    public void locomotiveGroupRemoved(final LocomotiveGroup group) {
        if (disableListener) {
            return;
        }
        updateLocomotiveGroups(ctx.getLocomotiveManager().getAllLocomotiveGroups());

    }

    @Override
    public void locomotiveGroupUpdated(final LocomotiveGroup group) {
        if (disableListener) {
            return;
        }
        updateLocomotiveGroups(ctx.getLocomotiveManager().getAllLocomotiveGroups());

    }

    private void resetLoco() {
        if (myLocomotive == null) {
            return;
        }
        final LocomotiveController locomotiveControl = ctx
                .getLocomotiveControl();
        locomotiveControl.removeLocomotiveChangeListener(myLocomotive, this);
        try {
            locomotiveControl.deactivateLoco(myLocomotive);
        } catch (Exception x) {

        }
        myLocomotive = null;

    }

    private void processMouseMovement(MouseEvent e) {
        if (myLocomotive == null) {
            return;
        }
        double i = (double) e.getY() / speedBar.getHeight();
        int drivingSteps = myLocomotive.getType().getDrivingSteps();
        int newSpeed = (int) ((1 - i) * (drivingSteps + 1));
        newSpeed = Math.max(0, Math.min(drivingSteps, newSpeed));
        if (newSpeed != myLocomotive.getCurrentSpeed()) {
            ctx.getLocomotiveControl().setSpeed(myLocomotive, newSpeed, myLocomotive.getCurrentFunctions());
        }
    }

    @Override
    public void failure(
            final ManagerException locomotiveManagerException) {
    }

    private class LocomotiveGroupSelectAction implements ItemListener {

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            if (ignoreGroupAndLocomotiveSelectionEvents) {
                return;
            }
            if (!isFree()) {
                return;
            }
            final LocomotiveGroup lg = (LocomotiveGroup) locomotiveGroupComboBoxModel
                    .getSelectedItem();
            final int idx = locomotiveGroupComboBox.getSelectedIndex();

            if (lg == null) {
                return;
            }
            locomotiveComboBox.setEnabled(false);
            locomotiveComboBoxModel.removeAllElements();
            for (final Locomotive l : lg.getLocomotives()) {
                locomotiveComboBoxModel.addElement(l);
            }
            locomotiveComboBox.setEnabled(true);

            locomotiveComboBox.setSelectedIndex(-1);
            try {
                resetLoco();
                locomotiveGroupComboBox.setSelectedIndex(idx);
            } catch (final ControllerException e1) {
                ctx.getMainApp().handleException(e1);
            }

        }
    }

    private class LocomotiveSelectAction implements ItemListener {

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            if (ignoreGroupAndLocomotiveSelectionEvents) {
                return;
            }
            try {
                if (locomotiveComboBox.getItemCount() == 0
                        || locomotiveComboBox.getSelectedIndex() == -1) {
                    resetLoco();
                    return;
                }
                if (isFree()) {

                    if (myLocomotive != null && ctx.getSession() != null) {
                        resetLoco();
                    }

                    myLocomotive = (Locomotive) locomotiveComboBox
                            .getSelectedItem();

                    ctx.getLocomotiveManager().setActiveLocomotive(number, myLocomotive);
                    final LocomotiveController locomotiveControl = ctx
                            .getLocomotiveControl();
                    locomotiveControl.addLocomotiveChangeListener(myLocomotive,
                            LocomotiveWidget.this);
                    locomotiveComboBox
                            .setBackground(UIConstants.DEFAULT_PANEL_COLOR);
                    lockButton.setBackground(UIConstants.DEFAULT_PANEL_COLOR);

                    final String locomotiveDescriptionToolTip = LocomotiveHelper
                            .getLocomotiveDescription(myLocomotive);

                    locomotiveComboBox.setToolTipText(locomotiveDescriptionToolTip);

                    updateFunctionButtons();
                    updateWidget();

                } else {
                    locomotiveGroupComboBox.setSelectedItem(myLocomotive
                            .getGroup());
                    locomotiveComboBox.setBackground(UIConstants.ERROR_COLOR);
                    locomotiveComboBox.setSelectedItem(myLocomotive);
                }
            } catch (final ControllerException e1) {
                ctx.getMainApp().handleException(e1);
            }
        }
    }

    private abstract class LocomotiveControlAction extends AbstractAction {

        private long time = 0;

        @Override
        public void actionPerformed(final ActionEvent e) {

            if (myLocomotive == null || !connectedToRailway) {
                return;
            }
            final LocomotiveController locomotiveControl = ctx
                    .getLocomotiveControl();
            doPerformAction(locomotiveControl, myLocomotive);
            if (time == 0) {
                time = System.currentTimeMillis();
            } else {
                time = 0;
            }
            speedBar.requestFocus();
        }

        protected abstract void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive);
    }

    private class LocomotiveFunctionAction extends LocomotiveControlAction {

        private final int function;

        public LocomotiveFunctionAction(final int function) {
            this.function = function;
        }

        @Override
        protected void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive) {
            final boolean state = functionToggleButtons.get(function)
                    .isSelected();

            final LocomotiveFunction locomotiveFunction = myLocomotive
                    .getFunction(function);
            final int deactivationDelay = locomotiveFunction != null ? locomotiveFunction
                    .getDeactivationDelay() : -1;
            locomotiveControl.setFunction(myLocomotive, function, state,
                    deactivationDelay);

            speedBar.requestFocus();
        }
    }

    private class LocomotiveAccelerateAction extends LocomotiveControlAction {


        @Override
        protected void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive) {
            locomotiveControl.increaseSpeed(myLocomotive);
        }
    }

    private class LocomotiveDeccelerateAction extends LocomotiveControlAction {


        @Override
        protected void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive) {
            locomotiveControl.decreaseSpeed(myLocomotive);
        }
    }

    private class LocomotiveToggleDirectionAction extends
            LocomotiveControlAction {

        @Override
        protected void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive) {
            if (Preferences.getInstance().getBooleanValue(
                    PreferencesKeys.STOP_ON_DIRECTION_CHANGE)
                    && myLocomotive.getCurrentSpeed() != 0) {
                locomotiveControl.setSpeed(myLocomotive, 0,
                        myLocomotive.getCurrentFunctions());
            }
            directionToggeled = true;
            locomotiveControl.toggleDirection(myLocomotive);
        }
    }

    private class StopAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (myLocomotive == null) {
                return;
            }
            final LocomotiveController locomotiveControl = ctx
                    .getLocomotiveControl();
            locomotiveControl.setSpeed(myLocomotive, 0,
                    myLocomotive.getCurrentFunctions());
            updateWidget();
            speedBar.requestFocus();
        }
    }

    private class ToggleDirectionAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (myLocomotive == null) {
                return;
            }
            final LocomotiveController locomotiveControl = ctx
                    .getLocomotiveControl();
            if (Preferences.getInstance().getBooleanValue(
                    PreferencesKeys.STOP_ON_DIRECTION_CHANGE)
                    && myLocomotive.getCurrentSpeed() != 0) {
                locomotiveControl.setSpeed(myLocomotive, 0,
                        myLocomotive.getCurrentFunctions());
            }
            directionToggeled = true;
            locomotiveControl.toggleDirection(myLocomotive);
            speedBar.requestFocus();
        }
    }

    private class LockAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (myLocomotive == null) {
                return;
            }
            final boolean lockButtonState = lockButton.isSelected();
            try {
                final LocomotiveController locomotiveControl = ctx
                        .getLocomotiveControl();
                if (lockButtonState) {
                    final boolean succeeded = locomotiveControl
                            .acquireLock(myLocomotive);
                    lockButton.setSelected(succeeded);
                } else {
                    if (locomotiveControl.isLockedByMe(myLocomotive)) {
                        final boolean succeeded = !locomotiveControl
                                .releaseLock(myLocomotive);
                        lockButton.setSelected(succeeded);
                    } else {
                        lockButton.setSelected(true);
                    }
                }
                speedBar.requestFocus();
            } catch (final ControllerException ex) {
                ctx.getMainApp().handleException(ex);
                lockButton.setSelected(lockButtonState);
            }
            updateWidget();
        }
    }

    private class MouseAction extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent e) {
            if (myLocomotive == null) {
                return;
            }
            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {

                if (ctx.isEditingMode()) {
                    if (!isFree()) {
                        return;
                    }
                    final LocomotiveController locomotiveControl = ctx
                            .getLocomotiveControl();

                    locomotiveControl.removeLocomotiveChangeListener(
                            myLocomotive, LocomotiveWidget.this);
                    new LocomotiveConfig(ctx, frame, myLocomotive, myLocomotive.getGroup());

                    locomotiveControl.addLocomotiveChangeListener(myLocomotive,
                            LocomotiveWidget.this);
                    locomotiveChanged(myLocomotive);
                } else {

                }
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                final ToggleDirectionAction a = new ToggleDirectionAction();
                a.actionPerformed(null);
            }
            updateWidget();
        }
    }

    private class WheelControl implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(final MouseWheelEvent e) {
            if (myLocomotive == null) {
                return;
            }
            AbstractAction a = null;
            switch (e.getWheelRotation()) {
                case -1:
                    a = new LocomotiveAccelerateAction();
                    break;
                case 1:
                    a = new LocomotiveDeccelerateAction();
                    break;
                default:
                    return;
            }
            a.actionPerformed(null);
        }
    }

}
