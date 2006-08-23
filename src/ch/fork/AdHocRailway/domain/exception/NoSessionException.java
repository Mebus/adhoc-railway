/*------------------------------------------------------------------------
 * 
 * <./domain/exception/NoSessionException.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:00 BST 2006
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


package ch.fork.AdHocRailway.domain.exception;

import ch.fork.AdHocRailway.domain.Constants;

public class NoSessionException extends ControlException {

    public NoSessionException() {
        super(Constants.ERR_NO_SESSION + "\nConnect to the SRCP-Server");
    }
}
