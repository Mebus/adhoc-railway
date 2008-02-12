/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchTypeComboBoxCellRenderer.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:14 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id:TurnoutTypeComboBoxCellRenderer.java 130 2008-02-01 20:23:34Z fork_ch $
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

package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.ui.ImageTools;

public class TurnoutTypeComboBoxCellRenderer
		extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel iconLabel =
				(JLabel) super.getListCellRendererComponent(list, value, index,
						isSelected, cellHasFocus);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		TurnoutType type = (TurnoutType) value;
		iconLabel.setText("");
		if (type.getTurnoutTypeEnum() == TurnoutTypes.DEFAULT) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_switch_small.png"));
		} else if (type.getTurnoutTypeEnum() == TurnoutTypes.DOUBLECROSS) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/double_cross_switch_small.png"));
		} else if (type.getTurnoutTypeEnum() == TurnoutTypes.THREEWAY) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/three_way_switch_small.png"));
		}
		return iconLabel;
	}
}
