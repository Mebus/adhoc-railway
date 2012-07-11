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

package ch.fork.AdHocRailway.technical.configuration.exporter;

import java.util.Map;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManger;
import ch.fork.AdHocRailway.technical.configuration.Preferences;

public class XMLExporter_0_2 {

	private StringBuffer				sb;
	private TurnoutManger		turnoutPersistence;
	private LocomotivePersistenceIface	locomotivePersistence;

	public XMLExporter_0_2(TurnoutManger turnoutPersistence,
			LocomotivePersistenceIface locomotivePersistence,
			RouteManager routePersistence) {
		this.turnoutPersistence = turnoutPersistence;
		this.locomotivePersistence = locomotivePersistence;

	}

	public String export() {
		sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\"?>\n");
		sb.append("<RailControl xmlns=\"http://www.fork.ch/RailControl\" "
				+ "ExporterVersion=\"0.2\">\n");

		exportSwitchConfiguration();
		exportLocomotiveConfiguration();
		exportPreferences();

		sb.append("</RailControl>");
		return sb.toString();
	}

	private void exportSwitchConfiguration() {
		sb.append("<SwitchConfiguration>\n");
		for (TurnoutGroup sg : turnoutPersistence.getAllTurnoutGroups()) {

			sb.append("<SwitchGroup name=\"" + sg.getName() + "\">\n");
			exportSwitches(sg);
			sb.append("</SwitchGroup>\n");
		}
		sb.append("</SwitchConfiguration>\n");
	}

	private void exportSwitches(TurnoutGroup sg) {
		for (Turnout s : sg.getTurnouts()) {
			sb.append("<Switch ");
			sb.append(" desc=\"" + s.getDescription() + "\" ");
			sb.append(" number=\"" + s.getNumber() + "\" ");
			sb.append(" type=\"" + s.getTurnoutType().getTypeName() + "\" ");
			sb.append(" defaultstate=\"" + s.getDefaultState() + "\" ");
			sb.append(" orientation=\"" + s.getOrientation() + "\" >\n");

			sb.append("<Address bus=\"" + s.getBus1() + "\" address=\""
					+ s.getAddress1() + "\" switched=\""
					+ s.isAddress1Switched() + "\" />\n");
			if (s.isThreeWay()) {
				sb.append("<Address bus=\"" + s.getBus2() + "\" address=\""
						+ s.getAddress2() + "\" switched=\""
						+ s.isAddress2Switched() + "\" />\n");
			}
			sb.append("</Switch>\n");
		}
	}

	private void exportLocomotiveConfiguration() {
		sb.append("<LocomotiveConfiguration>\n");
		for (LocomotiveGroup lg : locomotivePersistence
				.getAllLocomotiveGroups()) {

			sb.append("<LocomotiveGroup name=\"" + lg.getName() + "\">\n");
			for (Locomotive l : lg.getLocomotives()) {

				sb.append("<Locomotive name=\"" + l.getName() + "\" type=\""
						+ l.getLocomotiveType().getTypeName() + "\" bus=\""
						+ l.getBus() + "\" address=\"" + l.getAddress()
						+ "\" desc=\"" + l.getDescription() + "\" />\n");
			}
			sb.append("</LocomotiveGroup>\n");

		}
		sb.append("</LocomotiveConfiguration>\n");
	}

	private void exportPreferences() {
		sb.append("<GuiConfiguration>\n");
		Map<String, String> preferences = Preferences.getInstance()
				.getPreferences();
		for (String key : preferences.keySet()) {
			sb.append("<GuiConfigParameter ");
			sb.append(" name=\"" + key + "\"");
			sb.append(" value=\"" + preferences.get(key) + "\"");
			sb.append("/>\n");
		}
		sb.append("</GuiConfiguration>\n");
	}
}
