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

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.technical.configuration.ConfigurationException;

public class XMLExporter {
	private static Logger	logger	= Logger.getLogger(XMLExporter.class);

	public String export(double version,
			TurnoutPersistenceIface turnoutPersistence,
			LocomotivePersistenceIface locomotivePersistence,
			RoutePersistenceIface routePersistence)
			throws ConfigurationException {

		String export = "";
		if (version == 0.2) {
			export = new XMLExporter_0_2(turnoutPersistence,
					locomotivePersistence, routePersistence).export();
			logger.info("AdHoc-Railway Config Version 0.2 exported");
		} else if (version == 0.3) {
			export = new XMLExporter_0_3(turnoutPersistence,
					locomotivePersistence, routePersistence).export();
			logger.info("AdHoc-Railway Config Version 0.3 exported");
		} else {
			throw new ConfigurationException(
					Constants.ERR_VERSION_NOT_SUPPORTED);
		}
		return export;
	}
}
