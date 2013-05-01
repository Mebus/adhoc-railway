/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteException.java 262 2013-03-17 20:47:56Z fork_ch $
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

package ch.fork.AdHocRailway.domain.routes;


public class RouteException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4797784647018551199L;

	public RouteException() {
		// TODO Auto-generated constructor stub
	}

	public RouteException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RouteException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RouteException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
