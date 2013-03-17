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

package ch.fork.AdHocRailway.domain.locomotives;

public class LocomotiveManagerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6688667574674137647L;

	public LocomotiveManagerException() {
	}

	public LocomotiveManagerException(String message) {
		super(message);
	}

	public LocomotiveManagerException(Throwable cause) {
		super(cause);
	}

	public LocomotiveManagerException(String message, Throwable cause) {
		super(message, cause);
	}

}
