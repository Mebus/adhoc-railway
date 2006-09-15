/*------------------------------------------------------------------------
 * 
 * <./domain/configuration/XMLExporter.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:25 BST 2006
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


package ch.fork.AdHocRailway.technical.configuration;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.technical.configuration.exception.ConfigurationException;
import ch.fork.AdHocRailway.technical.configuration.exporter.XMLExporter_0_2;
import ch.fork.AdHocRailway.technical.configuration.exporter.XMLExporter_0_3;

public class XMLExporter {

    public static String export(double version) throws ConfigurationException {
        if (version == 0.3) {
            return XMLExporter_0_3.export();
        } else if(version == 0.2) {
            return XMLExporter_0_2.export();
        }
        throw new ConfigurationException(Constants.ERR_VERSION_NOT_SUPPORTED);
    }
}
