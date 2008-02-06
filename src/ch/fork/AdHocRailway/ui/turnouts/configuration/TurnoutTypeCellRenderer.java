/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchTypeCellRenderer.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:05 BST 2006
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


package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.ui.ImageTools;

public class TurnoutTypeCellRenderer implements TableCellRenderer {
    
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        TurnoutType type = (TurnoutType)value;
        if (type.getTurnoutTypeEnum() == TurnoutTypes.DEFAULT) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "switches/default_switch_small.png"));
        } else if (type.getTurnoutTypeEnum() == TurnoutTypes.DOUBLECROSS) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "switches/double_cross_switch_small.png"));
        } else if (type.getTurnoutTypeEnum() == TurnoutTypes.THREEWAY) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "switches/three_way_switch_small.png"));
        }
        return iconLabel;
    }
}