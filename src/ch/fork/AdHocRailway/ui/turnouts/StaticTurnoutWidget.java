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

package ch.fork.AdHocRailway.ui.turnouts;

import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;

public class StaticTurnoutWidget extends JPanel {

	private static final long		serialVersionUID	= 1L;

	private Turnout					turnout;

	private JLabel					numberLabel;

	private TurnoutCanvas			turnoutCanvas;

	private TurnoutState	state;

	public StaticTurnoutWidget(Turnout turnout, TurnoutState state) {
		this.turnout = turnout;
		this.state = state;

		initGUI();
	}

	private void initGUI() {
		
		setLayout(new FlowLayout());
		numberLabel = new JLabel(Integer.toString(turnout.getNumber()));
		turnoutCanvas = new TurnoutCanvas(turnout);
		turnoutCanvas.setTurnoutState(state);
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 30));
		add(numberLabel);
		add(turnoutCanvas);

	}
	public Turnout getTurnout() {
		return turnout;
	}
}
