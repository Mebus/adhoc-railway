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
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteException;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutWidget;

public class KeyTrackControl extends SimpleInternalFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3052109699874529256L;

	private StringBuffer enteredNumberKeys;

	private JPanel switchesHistory;

	private final LinkedList<Object> historyStack;

	private final LinkedList<JPanel> historyWidgets;

	public boolean routeMode;

	public boolean changedSwitch = false;

	public boolean changedRoute = false;

	public static final int HISTORY_LENGTH = 5;

	private JScrollPane switchesHistoryPane;

	private ThreeDigitDisplay digitDisplay;

	public KeyTrackControl() {
		super("Track Control / History");
		this.historyStack = new LinkedList<Object>();
		this.historyWidgets = new LinkedList<JPanel>();
		enteredNumberKeys = new StringBuffer();
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		final JPanel segmentPanelNorth = initSegmentPanel();
		switchesHistory = new JPanel();
		final JPanel sh1 = new JPanel(new BorderLayout());

		switchesHistory.setLayout(new MigLayout("insets 5"));

		switchesHistoryPane = new JScrollPane(switchesHistory,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sh1.add(switchesHistoryPane, BorderLayout.CENTER);

		add(segmentPanelNorth, BorderLayout.NORTH);
		add(sh1, BorderLayout.CENTER);
	}

	private JPanel initSegmentPanel() {
		digitDisplay = new ThreeDigitDisplay();
		final JPanel p = new JPanel(new BorderLayout());
		p.add(digitDisplay, BorderLayout.WEST);
		return p;
	}

	private void initKeyboardActions() {
		for (int i = 0; i <= 10; i++) {
			registerKeyboardAction(new NumberEnteredAction(),
					Integer.toString(i),
					KeyStroke.getKeyStroke(Integer.toString(i)),
					WHEN_IN_FOCUSED_WINDOW);
			registerKeyboardAction(new NumberEnteredAction(),
					Integer.toString(i),
					KeyStroke.getKeyStroke("NUMPAD" + Integer.toString(i)),
					WHEN_IN_FOCUSED_WINDOW);

		}
		final KeyBoardLayout kbl = Preferences.getInstance()
				.getKeyBoardLayout();
		final InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		getActionMap().put("RouteNumberEntered", new PeriodEnteredAction());
		kbl.assignKeys(inputMap, "RouteNumberEntered");
		getActionMap().put("CurvedLeft", new CurvedLeftAction());
		kbl.assignKeys(inputMap, "CurvedLeft");
		getActionMap().put("CurvedRight", new CurvedRightAction());
		kbl.assignKeys(inputMap, "CurvedRight");
		getActionMap().put("Straight", new StraightAction());
		kbl.assignKeys(inputMap, "Straight");
		getActionMap().put("EnableRoute", new EnableRouteAction());
		kbl.assignKeys(inputMap, "EnableRoute");
		getActionMap().put("DisableRoute", new DisableRouteAction());
		kbl.assignKeys(inputMap, "DisableRoute");
	}

	private void updateHistory(final Object obj) {
		if (historyStack.size() == HISTORY_LENGTH) {
			historyStack.removeFirst();
			historyWidgets.removeFirst();
		}
		if (!historyStack.isEmpty() && historyStack.getFirst().equals(obj)) {
			historyStack.removeLast();
			historyWidgets.removeLast();
		}
		historyStack.addLast(obj);
		JPanel w = null;

		if (obj instanceof Turnout) {
			final Turnout turnout = (Turnout) obj;
			w = new TurnoutWidget(turnout, false, true);
		} else if (obj instanceof Route) {
			w = new RouteWidget((Route) obj);
		} else {
			return;
		}
		historyWidgets.addLast(w);
		updateHistory();
	}

	private void updateHistory() {
		switchesHistory.removeAll();

		for (final JPanel p : historyWidgets) {
			switchesHistory.add(p, "wrap");
		}
		revalidate();
		repaint();
	}

	private class NumberEnteredAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4818198896180938380L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			enteredNumberKeys.append(e.getActionCommand());
			final String switchNumberAsString = enteredNumberKeys.toString();
			final int switchNumber = Integer.parseInt(switchNumberAsString);
			if (switchNumber > 999) {
				digitDisplay.reset();
				enteredNumberKeys = new StringBuffer();
				return;
			}
			digitDisplay.setNumber(switchNumber);
		}
	}

	private class PeriodEnteredAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6709249386564202875L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			routeMode = true;
			digitDisplay.setPeriod(true);
		}
	}

	private abstract class SwitchingAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8783785027663679688L;

		@Override
		public void actionPerformed(final ActionEvent e) {

			try {
				final RouteControlIface routeControl = AdHocRailway
						.getInstance().getRouteControl();
				final TurnoutControlIface turnoutControl = AdHocRailway
						.getInstance().getTurnoutControl();

				final String enteredNumberAsString = enteredNumberKeys
						.toString();
				if (enteredNumberKeys.toString().equals("")) {
					if (historyStack.size() == 0) {
						return;
					}
					final Object obj = historyStack.removeFirst();
					if (obj instanceof Turnout) {
						final Turnout t = (Turnout) obj;
						turnoutControl.setDefaultState(t);
					} else if (obj instanceof Route) {
						final Route r = (Route) obj;
						routeControl.disableRoute(r);
					} else {
						return;
					}
					historyWidgets.removeFirst();
					updateHistory();
				} else {
					final int enteredNumber = Integer
							.parseInt(enteredNumberAsString);
					if (routeMode) {
						handleRouteChange(e, enteredNumber);
					} else {
						handleSwitchChange(e, enteredNumber);
					}
				}

			} catch (final RouteException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			} catch (final TurnoutException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
			enteredNumberKeys = new StringBuffer();
			routeMode = false;
			digitDisplay.reset();
		}

		private void handleSwitchChange(final ActionEvent e,
				final int enteredNumber) throws TurnoutException {
			final TurnoutManager turnoutPersistence = AdHocRailway
					.getInstance().getTurnoutPersistence();
			Turnout searchedTurnout = null;
			searchedTurnout = turnoutPersistence
					.getTurnoutByNumber(enteredNumber);
			if (searchedTurnout == null) {
				return;
			}
			final TurnoutControlIface turnoutControl = AdHocRailway
					.getInstance().getTurnoutControl();

			if (this instanceof CurvedLeftAction) {
				turnoutControl.setCurvedLeft(searchedTurnout);
			} else if (this instanceof StraightAction) {
				turnoutControl.setStraight(searchedTurnout);
			} else if (this instanceof CurvedRightAction) {
				turnoutControl.setCurvedRight(searchedTurnout);
			} else if (this instanceof EnableRouteAction) {
				if (!searchedTurnout.isThreeWay()) {
					turnoutControl.setNonDefaultState(searchedTurnout);
				}
			} else if (this instanceof DisableRouteAction) {
				if (!searchedTurnout.isThreeWay()) {
					turnoutControl.setDefaultState(searchedTurnout);
				}
			}
			updateHistory(searchedTurnout);
		}

		private void handleRouteChange(final ActionEvent e,
				final int enteredNumber) throws TurnoutException,
				RouteException {
			Route searchedRoute = null;

			final RouteControlIface routeControl = AdHocRailway.getInstance()
					.getRouteControl();
			final RouteManager routePersistence = AdHocRailway.getInstance()
					.getRoutePersistence();
			searchedRoute = routePersistence.getRouteByNumber(enteredNumber);
			if (searchedRoute == null) {
				return;
			}
			if (this instanceof EnableRouteAction) {
				routeControl.enableRoute(searchedRoute);
			} else if (this instanceof DisableRouteAction) {
				routeControl.disableRoute(searchedRoute);
			}
			updateHistory(searchedRoute);
		}
	}

	private class CurvedLeftAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5676902063321467852L;
	}

	private class StraightAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2339006950893044415L;
	}

	private class CurvedRightAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7285117051054231241L;
	}

	private class EnableRouteAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5376121297997351343L;
	}

	private class DisableRouteAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4179628128437613997L;
	}

}
