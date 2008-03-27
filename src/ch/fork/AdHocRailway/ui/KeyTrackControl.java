/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import ch.fork.AdHocRailway.domain.ControlException;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.turnouts.StaticTurnoutWidget;

public class KeyTrackControl extends SimpleInternalFrame {

	private StringBuffer			enteredNumberKeys;

	private JPanel					switchesHistory;

	private Deque<Object>			historyStack;

	private Deque<JPanel>			historyWidgets;

	private TurnoutControlIface		turnoutControl		= AdHocRailway
																.getInstance()
																.getTurnoutControl();

	private TurnoutPersistenceIface	turnoutPersistence	= AdHocRailway
																.getInstance()
																.getTurnoutPersistence();

	private RouteControlIface		routeControl		= AdHocRailway
																.getInstance()
																.getRouteControl();

	private RoutePersistenceIface	routePersistence	= AdHocRailway
																.getInstance()
																.getRoutePersistence();

	public boolean					routeMode;

	public boolean					changedSwitch		= false;

	public boolean					changedRoute		= false;

	public static final int			HISTORY_LENGTH		= 5;

	private JScrollPane				switchesHistoryPane;

	private ThreeDigitDisplay		digitDisplay;

	public KeyTrackControl() {
		super("Track Control / History");
		this.historyStack = new ArrayDeque<Object>();
		this.historyWidgets = new ArrayDeque<JPanel>();
		enteredNumberKeys = new StringBuffer();
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		JPanel segmentPanelNorth = initSegmentPanel();
		switchesHistory = new JPanel();
		JPanel sh1 = new JPanel(new BorderLayout());

		switchesHistory.setLayout(new GridLayout(HISTORY_LENGTH, 1));

		switchesHistoryPane = new JScrollPane(switchesHistory,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sh1.add(switchesHistoryPane, BorderLayout.CENTER);

		add(segmentPanelNorth, BorderLayout.NORTH);
		add(sh1, BorderLayout.CENTER);
	}

	private JPanel initSegmentPanel() {
		digitDisplay = new ThreeDigitDisplay();
		JPanel p = new JPanel(new BorderLayout());
		p.add(digitDisplay, BorderLayout.WEST);
		return p;
	}

	private void initKeyboardActions() {
		for (int i = 0; i <= 10; i++) {
			registerKeyboardAction(new NumberEnteredAction(), Integer
					.toString(i), KeyStroke.getKeyStroke(Integer.toString(i)),
					WHEN_IN_FOCUSED_WINDOW);
			registerKeyboardAction(new NumberEnteredAction(), Integer
					.toString(i), KeyStroke.getKeyStroke("NUMPAD"
					+ Integer.toString(i)), WHEN_IN_FOCUSED_WINDOW);

		}
		registerKeyboardAction(new NumberEnteredAction(), ".", KeyStroke
				.getKeyStroke(KeyEvent.VK_PERIOD, 0), WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new SwitchingAction(), "\\", KeyStroke
				.getKeyStroke(92, 0), WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new NumberEnteredAction(), ".", KeyStroke
				.getKeyStroke(110, 0), WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new SwitchingAction(), "\n", KeyStroke
				.getKeyStroke("ENTER"), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "+", KeyStroke
				.getKeyStroke(KeyEvent.VK_ADD, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "bs", KeyStroke
				.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "/", KeyStroke
				.getKeyStroke(KeyEvent.VK_DIVIDE, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "*", KeyStroke
				.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "-", KeyStroke
				.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), WHEN_IN_FOCUSED_WINDOW);

	}

	private void updateHistory(Object obj) {
		if (historyStack.size() == HISTORY_LENGTH) {
			historyStack.removeLast();
			historyWidgets.removeLast();

		}
		historyStack.push(obj);
		JPanel w = null;
		if (obj instanceof Turnout) {
			Turnout turnout = (Turnout) obj;

			w = new StaticTurnoutWidget(turnout, turnoutControl
					.getTurnoutState(turnout));
		} else if (obj instanceof Route) {
			w = new RouteWidget((Route) obj);
		} else {
			return;
		}
		historyWidgets.push(w);
		updateHistory();
	}

	private void updateHistory() {
		switchesHistory.removeAll();

		for (JPanel p : historyWidgets) {
			switchesHistory.add(p);
		}
		revalidate();
		repaint();
	}

	private class NumberEnteredAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand() == ".") {
				routeMode = true;
				digitDisplay.setPeriod(true);
			} else {
				enteredNumberKeys.append(e.getActionCommand());
				String switchNumberAsString = enteredNumberKeys.toString();
				int switchNumber = Integer.parseInt(switchNumberAsString);
				if (switchNumber > 999) {
					digitDisplay.reset();
					enteredNumberKeys = new StringBuffer();
					return;
				}
				digitDisplay.setNumber(switchNumber);
			}
		}
	}

	private class SwitchingAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {

			try {

				String enteredNumberAsString = enteredNumberKeys.toString();
				if (enteredNumberKeys.toString().equals("")) {
					if (historyStack.size() == 0)
						return;
					Object obj = historyStack.pop();
					if (obj instanceof Turnout) {
						Turnout t = (Turnout) obj;
						turnoutControl.setDefaultState(t);
					} else if (obj instanceof Route) {
						Route r = (Route) obj;
						routeControl.disableRoute(r);
					} else {
						return;
					}
					historyWidgets.pop();
					updateHistory();
				} else {
					int enteredNumber = Integer.parseInt(enteredNumberAsString);
					if (routeMode) {
						handleRouteChange(e, enteredNumber);
					} else {
						handleSwitchChange(e, enteredNumber);
					}
				}

				enteredNumberKeys = new StringBuffer();
				routeMode = false;
				digitDisplay.reset();
			} catch (ControlException e1) {

				enteredNumberKeys = new StringBuffer();
				digitDisplay.reset();
				ExceptionProcessor.getInstance().processException(e1);
			}
		}

		private void handleSwitchChange(ActionEvent e, int enteredNumber)
				throws TurnoutException {
			Turnout searchedTurnout = null;

			searchedTurnout = turnoutPersistence
					.getTurnoutByNumber(enteredNumber);
			if (searchedTurnout == null) {
				return;
			}

			if (e.getActionCommand().equals("/")) {
				turnoutControl.setCurvedLeft(searchedTurnout);
			} else if (e.getActionCommand().equals("*")) {
				turnoutControl.setStraight(searchedTurnout);
			} else if (e.getActionCommand().equals("-")) {
				turnoutControl.setCurvedRight(searchedTurnout);
			} else if (e.getActionCommand().equals("+")) {
				if (!searchedTurnout.isThreeWay()) {
					turnoutControl.setNonDefaultState(searchedTurnout);
				}
			} else if (e.getActionCommand().equals("bs")) {
				if (!searchedTurnout.isThreeWay()) {
					turnoutControl.setNonDefaultState(searchedTurnout);
				}
			} else if (e.getActionCommand().equals("\n")) {
				turnoutControl.setDefaultState(searchedTurnout);
			}
			updateHistory(searchedTurnout);
		}

		private void handleRouteChange(ActionEvent e, int enteredNumber)
				throws TurnoutException {
			Route searchedRoute = null;

			searchedRoute = routePersistence.getRouteByNumber(enteredNumber);
			if (searchedRoute == null) {
				return;
			}
			if (e.getActionCommand().equals("+")
					|| e.getActionCommand().equals("bs")) {
				routeControl.enableRoute(searchedRoute);
			} else if (e.getActionCommand().equals("\n")) {
				routeControl.disableRoute(searchedRoute);
			} else if (e.getActionCommand().equals("\\")) {
				routeControl.enableRoute(searchedRoute);
			}
			updateHistory(searchedRoute);
		}
	}

}
