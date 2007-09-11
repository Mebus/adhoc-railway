/*------------------------------------------------------------------------
 * 
 * <./ui/switches/SwitchWidget.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:35 BST 2006
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

package ch.fork.AdHocRailway.ui.switches;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.locking.LockChangeListener;
import ch.fork.AdHocRailway.domain.turnouts.DefaultSwitch;
import ch.fork.AdHocRailway.domain.turnouts.DoubleCrossSwitch;
import ch.fork.AdHocRailway.domain.turnouts.Switch;
import ch.fork.AdHocRailway.domain.turnouts.SwitchChangeListener;
import ch.fork.AdHocRailway.domain.turnouts.SwitchGroup;
import ch.fork.AdHocRailway.domain.turnouts.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.exception.SwitchException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.switches.canvas.DefaultSwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.DoubleCrossSwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.SwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.ThreeWaySwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.configuration.SwitchConfig;

public class SwitchWidget extends JPanel implements SwitchChangeListener,
		LockChangeListener {
	private TurnoutControl switchControl;

	private static final long serialVersionUID = 1L;

	private Switch mySwitch;

	private JLabel numberLabel;

	private JLabel descLabel;

	private SwitchGroup switchGroup;

	private SwitchCanvas switchCanvas;

	private GridBagLayout switchWidgetLayout;

	private GridBagConstraints switchWidgetConstraints;

	private JFrame frame;

	public SwitchWidget(Switch aSwitch, SwitchGroup switchGroup, JFrame frame) {
		mySwitch = aSwitch;
		this.switchGroup = switchGroup;
		this.frame = frame;
		this.switchControl = TurnoutControl.getInstance();
		initGUI();
		switchControl.addSwitchChangeListener(aSwitch, this);
	}

	private void initGUI() {
		switchCanvas = null;
		if (mySwitch instanceof DoubleCrossSwitch) {
			switchCanvas = new DoubleCrossSwitchCanvas(mySwitch);
		} else if (mySwitch instanceof DefaultSwitch) {
			switchCanvas = new DefaultSwitchCanvas(mySwitch);
		} else if (mySwitch instanceof ThreeWaySwitch) {
			switchCanvas = new ThreeWaySwitchCanvas(mySwitch);
		}
		switchCanvas.addMouseListener(new MouseAction());
		addMouseListener(new MouseAction());

		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		switchWidgetLayout = new GridBagLayout();
		switchWidgetConstraints = new GridBagConstraints();
		setLayout(switchWidgetLayout);
		switchWidgetConstraints.insets = new Insets(5, 5, 5, 5);
		switchWidgetConstraints.gridx = 0;
		numberLabel = new JLabel(Integer.toString(mySwitch.getNumber()));
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 30));
		switchWidgetLayout.setConstraints(numberLabel, switchWidgetConstraints);
		add(numberLabel);
		switchWidgetConstraints.gridx = 1;
		descLabel = new JLabel(mySwitch.getDesc());
		switchWidgetLayout.setConstraints(descLabel, switchWidgetConstraints);
		add(descLabel);
		switchWidgetConstraints.gridx = 0;
		switchWidgetConstraints.gridy = 1;
		switchWidgetConstraints.gridwidth = 2;
		switchWidgetLayout
				.setConstraints(switchCanvas, switchWidgetConstraints);
		add(switchCanvas);

	}

	public void switchChanged(Switch changedSwitch) {
		if (mySwitch.equals(changedSwitch)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					numberLabel.setText(Integer.toString(mySwitch.getNumber()));
					descLabel.setText(mySwitch.getDesc());
					SwitchWidget.this.revalidate();
					SwitchWidget.this.repaint();
				}
			});
		}
	}

	public void lockChanged(ControlObject changedLock) {
		if (changedLock instanceof Switch) {
			Switch changedSwitch = (Switch) changedLock;
			switchChanged(changedSwitch);
		}
	}

	private class MouseAction extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			try {
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					switchControl.toggle(mySwitch);
				} else if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON3) {
					displaySwitchConfig();
				}
			} catch (SwitchException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}

		private void displaySwitchConfig() {
			SwitchConfig switchConf = new SwitchConfig(frame, mySwitch.clone());
			if (switchConf.isOkPressed()) {
				switchControl.removeSwitchChangeListener(mySwitch);
				switchGroup.removeSwitch(mySwitch);
				switchControl.unregisterSwitch(mySwitch);
				switchControl.removeSwitchChangeListener(mySwitch);

				Switch newSwitch = switchConf.getSwitch();
				switchGroup.addSwitch(newSwitch);
				switchControl.registerSwitch(newSwitch);
				switchControl.addSwitchChangeListener(newSwitch,
						SwitchWidget.this);

				mySwitch = newSwitch;

				remove(switchCanvas);
				switchWidgetConstraints.gridx = 0;
				switchWidgetConstraints.gridy = 1;
				switchWidgetConstraints.gridwidth = 2;
				switchCanvas = null;
				if (mySwitch instanceof DoubleCrossSwitch) {
					switchCanvas = new DoubleCrossSwitchCanvas(mySwitch);
				} else if (mySwitch instanceof DefaultSwitch) {
					switchCanvas = new DefaultSwitchCanvas(mySwitch);
				} else if (mySwitch instanceof ThreeWaySwitch) {
					switchCanvas = new ThreeWaySwitchCanvas(mySwitch);
				}
				switchWidgetLayout.setConstraints(switchCanvas,
						switchWidgetConstraints);
				add(switchCanvas);
				switchCanvas.addMouseListener(new MouseAction());
				switchChanged(mySwitch);
			}
		}
	}

	public Switch getMySwitch() {
		return mySwitch;
	}
}
