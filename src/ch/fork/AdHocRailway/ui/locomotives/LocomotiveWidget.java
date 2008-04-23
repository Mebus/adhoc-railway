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

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import ch.fork.AdHocRailway.domain.locking.LockChangeListener;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveChangeListener;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfig;

import com.jgoodies.binding.list.ArrayListModel;

public class LocomotiveWidget extends JPanel implements
		LocomotiveChangeListener, LockChangeListener {
	private static final long			serialVersionUID		= 1L;

	private JComboBox					locomotiveComboBox;

	private JComboBox					locomotiveGroupComboBox;

	private JLabel						desc;

	private JProgressBar				speedBar;

	private JButton						increaseSpeed;

	private JButton						decreaseSpeed;

	private JButton						stopButton;

	private JButton						directionButton;

	private LockToggleButton			lockButton;

	private Locomotive					myLocomotive;

	private int							accelerateKey, deccelerateKey,
			toggleDirectionKey;

	private FunctionToggleButton[]		functionToggleButtons;

	private LocomotiveControlface		locomotiveControl		= AdHocRailway
																		.getInstance()
																		.getLocomotiveControl();

	private LocomotivePersistenceIface	locomotivePersistence	= AdHocRailway
																		.getInstance()
																		.getLocomotivePersistence();

	private LocomotiveSelectAction		locomotiveSelectAction;

	private LocomotiveGroupSelectAction	groupSelectAction;

	private JFrame						frame;

	private Object	defaultDisabledComboColor;

	public LocomotiveWidget(int accelerateKey, int deccelerateKey,
			int toggleDirectionKey, JFrame frame) {
		super();
		this.accelerateKey = accelerateKey;
		this.deccelerateKey = deccelerateKey;
		this.toggleDirectionKey = toggleDirectionKey;
		this.frame = frame;
		defaultDisabledComboColor = UIManager.get("ComboBox.disabledForeground");
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setPreferredSize(new Dimension(200, 250));
		JPanel selectionPanel = initSelectionPanel();
		JPanel controlPanel = initControlPanel();
		JPanel centerPanel = new JPanel(new BorderLayout());
		if (myLocomotive != null) {
			desc = new JLabel(myLocomotive.getDescription(),
					SwingConstants.CENTER);
		} else {
			desc = new JLabel("", SwingConstants.CENTER);
		}

		addMouseListener(new MouseAction());
		centerPanel.add(controlPanel, BorderLayout.CENTER);
		centerPanel.add(desc, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(selectionPanel, BorderLayout.NORTH);

		addMouseWheelListener(new WheelControl());

	}

	private JPanel initSelectionPanel() {
		JPanel selectionPanel = new JPanel(new BorderLayout(5, 5));
		locomotiveGroupComboBox = new JComboBox();
		locomotiveGroupComboBox.setFocusable(false);
		groupSelectAction = new LocomotiveGroupSelectAction();
		locomotiveComboBox = new JComboBox();
		locomotiveComboBox.setFocusable(false);
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
		directionButton = new JButton(
				createImageIcon("locomotives/forward.png"));
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
		registerKeyboardAction(new LocomotiveControlAction(), "accelerate",
				KeyStroke.getKeyStroke(accelerateKey, 0),
				WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new LocomotiveControlAction(), "deccelerate",
				KeyStroke.getKeyStroke(deccelerateKey, 0),
				WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new LocomotiveControlAction(),
				"toggle_direction", KeyStroke.getKeyStroke(toggleDirectionKey,
						0), WHEN_IN_FOCUSED_WINDOW);
	}

	private boolean isFree() {
		if (myLocomotive == null)
			return true;
		if (locomotiveControl.getCurrentSpeed(myLocomotive) == 0) {
			if (locomotiveControl.isLocked(myLocomotive)) {
				if (locomotiveControl.isLockedByMe(myLocomotive))
					return false;
				else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			if (locomotiveControl.isLocked(myLocomotive)) {
				if (locomotiveControl.isLockedByMe(myLocomotive))
					return false;
				else {
					return true;
				}
			} else {
				return false;
			}
		}
	}

	public void updateLocomotiveGroups() {
		locomotiveComboBox.removeActionListener(locomotiveSelectAction);
		locomotiveGroupComboBox.removeActionListener(groupSelectAction);

		locomotiveGroupComboBox.addItem("All");
		locomotiveComboBox.addItem("");
		for (LocomotiveGroup lg : locomotivePersistence
				.getAllLocomotiveGroups()) {
			locomotiveGroupComboBox.addItem(lg);
			for (Locomotive l : lg.getLocomotives()) {
				locomotiveComboBox.addItem(l);
			}
		}

		locomotiveGroupComboBox.setSelectedIndex(0);
		if (myLocomotive != null)
			locomotiveComboBox.setSelectedItem(myLocomotive);
		else
			locomotiveComboBox.setSelectedIndex(0);

		locomotiveComboBox.addActionListener(locomotiveSelectAction);
		locomotiveGroupComboBox.addActionListener(groupSelectAction);
	}

	private class LocomotiveGroupSelectAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			locomotiveComboBox.removeAllItems();
			if (locomotiveGroupComboBox.getSelectedIndex() == 0) {
				ArrayListModel<Locomotive> sl = locomotivePersistence
						.getAllLocomotives();

				locomotiveComboBox.addItem("");
				for (Locomotive l : sl) {
					locomotiveComboBox.addItem(l);
				}
			} else {
				LocomotiveGroup lg = (LocomotiveGroup) locomotiveGroupComboBox
						.getSelectedItem();

				locomotiveComboBox.addItem("");
				for (Locomotive l : lg.getLocomotives()) {
					locomotiveComboBox.addItem(l);
				}
			}
			locomotiveComboBox.setSelectedIndex(0);
			locomotiveComboBox.revalidate();
			locomotiveComboBox.repaint();
		}
	}

	private class LocomotiveSelectAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (locomotiveComboBox.getItemCount() == 0
					|| locomotiveComboBox.getSelectedIndex() == 0) {
				myLocomotive = null;
				return;
			}
			if (isFree()) {
				locomotiveComboBox
						.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
				lockButton.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
				myLocomotive = (Locomotive) locomotiveComboBox
						.getSelectedItem();
				locomotiveControl.addLocomotiveChangeListener(myLocomotive,
						LocomotiveWidget.this);
				updateWidget();
				speedBar.requestFocus();
			} else {
				locomotiveGroupComboBox.setSelectedItem(myLocomotive
						.getLocomotiveGroup());
				locomotiveComboBox.setBackground(UIConstants.ERROR_COLOR);
				locomotiveComboBox.setSelectedItem(myLocomotive);
			}
		}
	}

	protected void updateWidget() {
		if (myLocomotive == null)
			return;
		double speedInPercent = ((double) locomotiveControl
				.getCurrentSpeed(myLocomotive))
				/ ((double) myLocomotive.getLocomotiveType().getDrivingSteps());
		if (speedInPercent > 0.9) {
			speedBar.setForeground(new Color(255, 0, 0));
		} else if (speedInPercent > 0.7) {
			speedBar.setForeground(new Color(255, 255, 0));
		} else {
			speedBar.setForeground(new Color(0, 255, 0));
		}
		speedBar.setMinimum(0);
		speedBar.setMaximum(myLocomotive.getLocomotiveType().getDrivingSteps());
		speedBar.setValue(locomotiveControl.getCurrentSpeed(myLocomotive));
		boolean functions[] = locomotiveControl.getFunctions(myLocomotive);
		for (int i = 0; i < functions.length; i++) {
			functionToggleButtons[i].setSelected(functions[i]);
		}
		switch (locomotiveControl.getDirection(myLocomotive)) {
		case FORWARD:
			directionButton.setIcon(createImageIcon("locomotives/forward.png"));
			break;
		case REVERSE:
			directionButton.setIcon(createImageIcon("locomotives/back.png"));
			break;
		default:
			directionButton.setIcon(createImageIcon("locomotives/forward.png"));
		}

		try {
		boolean locked = locomotiveControl.isLocked(myLocomotive);
		lockButton.setSelected(locked);
		if (locked) {
			if (locomotiveControl.isLockedByMe(myLocomotive)) {
				lockButton.setSelectedIcon(ImageTools
						.createImageIcon("locomotives/locked_by_me.png"));

			} else {
				lockButton.setSelectedIcon(ImageTools
						.createImageIcon("locomotives/locked_by_enemy.png"));
			}
		}
		if (isFree()) {
			locomotiveGroupComboBox.setEnabled(true);
			locomotiveComboBox.setEnabled(true);
		} else {
			UIManager.put("ComboBox.disabledForeground", new ColorUIResource(Color.BLACK));
			locomotiveGroupComboBox.setEnabled(false);
			locomotiveComboBox.setEnabled(false);
		}
		} catch(LocomotiveException ex) {
			//ignore here
		}
		setPreferredSize(new Dimension(200, 250));
		revalidate();
		repaint();
	}

	private class LocomotiveFunctionAction extends AbstractAction {
		private int	function;

		public LocomotiveFunctionAction(int function) {
			this.function = function;
		}

		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			try {
				boolean[] functions = locomotiveControl
						.getFunctions(myLocomotive);
				functions[function] = functionToggleButtons[function]
						.isSelected();
				locomotiveControl.setFunctions(myLocomotive, functions);
			} catch (LocomotiveException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
			speedBar.requestFocus();
		}
	}

	private class LocomotiveControlAction extends AbstractAction {

		private long	time	= 0;

		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			try {
				if (e.getActionCommand().equals("accelerate")) {
					locomotiveControl.increaseSpeed(myLocomotive);
				} else if (e.getActionCommand().equals("deccelerate")) {
					locomotiveControl.decreaseSpeed(myLocomotive);
				} else if (e.getActionCommand().equals("toggle_direction")) {
					locomotiveControl.toggleDirection(myLocomotive);
				}
				if (time == 0) {
					time = System.currentTimeMillis();
				} else {
					time = 0;
				}
				SwingUtilities.invokeLater(new LocomotiveWidgetUpdater());
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}

		}
	}

	private class StopAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			try {
				locomotiveControl.setSpeed(myLocomotive, 0, null);
				updateWidget();
				speedBar.requestFocus();
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}
		}
	}

	private class ToggleDirectionAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			try {
				locomotiveControl.toggleDirection(myLocomotive);
				speedBar.requestFocus();
				updateWidget();
			} catch (LocomotiveException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}
	}

	private class IncreaseSpeedAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			try {
				locomotiveControl.increaseSpeed(myLocomotive);
				updateWidget();
				speedBar.requestFocus();
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}
		}
	}

	private class DecreaseSpeedAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			try {
				locomotiveControl.decreaseSpeed(myLocomotive);
				updateWidget();
				speedBar.requestFocus();
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}
		}
	}

	private class LockAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;

			if (lockButton.isSelected()) {
				boolean succeeded = locomotiveControl.acquireLock(myLocomotive);
				lockButton.setSelected(succeeded);
			} else {
				if (locomotiveControl.isLockedByMe(myLocomotive)) {
					boolean succeeded = !locomotiveControl
							.releaseLock(myLocomotive);
					lockButton.setSelected(succeeded);
				} else {
					lockButton.setSelected(true);
				}
			}
			updateWidget();
			speedBar.requestFocus();
		}

	}

	private class MouseAction extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (myLocomotive == null)
				return;
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
				LocomotiveConfig locomotiveConfig = new LocomotiveConfig(frame,
						myLocomotive);
				if (locomotiveConfig.isOkPressed()) {
					locomotivePersistence.updateLocomotive(myLocomotive);
				}
				locomotiveChanged(myLocomotive);
				locomotiveControl.update();
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				ToggleDirectionAction a = new ToggleDirectionAction();
				a.actionPerformed(null);
			}
			updateWidget();
		}
	}

	private class WheelControl implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (myLocomotive == null)
				return;
			AbstractAction a = null;
			switch (e.getWheelRotation()) {
			case -1:
				a = new IncreaseSpeedAction();
				break;
			case 1:
				a = new DecreaseSpeedAction();
				break;
			default:
				return;
			}
			a.actionPerformed(null);
		}
	}

	public Locomotive getMyLocomotive() {
		return myLocomotive;
	}

	public void locomotiveChanged(Locomotive changedLocomotive) {
		if (myLocomotive == null) // this widget does not have a selected
			// locomotive yet
			return;
		if (myLocomotive.equals(changedLocomotive)) {
			SwingUtilities.invokeLater(new LocomotiveWidgetUpdater());
		}
	}

	private class LocomotiveWidgetUpdater implements Runnable {
		public void run() {
			updateWidget();
		}
	}

	public void lockChanged(Object object) {
		if (object instanceof Locomotive) {
			locomotiveChanged((Locomotive) object);
		}
	}
}
