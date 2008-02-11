/*------------------------------------------------------------------------
 * 
 * <./ui/switches/canvas/Segment7.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:40 BST 2006
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

package ch.fork.AdHocRailway.ui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

public class Segment7
		extends JPanel {
	private int		digit;
	private boolean	hasfocus;
	private boolean	displayPeriod	= false;

	private int[]	periodx			= { 0, 0, 1, 1 };
	private int[]	periody			= { 19, 18, 18, 19 };

	private int[][]	polysx			= { { 1, 2, 8, 9, 8, 2 }, // Segment 0
			{ 9, 10, 10, 9, 8, 8 }, // Segment 1
			{ 9, 10, 10, 9, 8, 8 }, // Segment 2
			{ 1, 2, 8, 9, 8, 2 }, // Segment 3
			{ 1, 2, 2, 1, 0, 0 }, // Segment 4
			{ 1, 2, 2, 1, 0, 0 }, // Segment 5
			{ 1, 2, 8, 9, 8, 2 }, // Segment 6
									};
	private int[][]	polysy			= { { 1, 0, 0, 1, 2, 2 }, // Segment 0
			{ 1, 2, 8, 9, 8, 2 }, // Segment 1
			{ 9, 10, 16, 17, 16, 10 }, // Segment 2
			{ 17, 16, 16, 17, 18, 18 }, // Segment 3
			{ 9, 10, 16, 17, 16, 10 }, // Segment 4
			{ 1, 2, 8, 9, 8, 2 }, // Segment 5
			{ 9, 8, 8, 9, 10, 10 }, // Segment 6
									};
	private int[][]	digits			= { { 1, 1, 1, 1, 1, 1, 0 }, // Ziffer 0
			{ 0, 1, 1, 0, 0, 0, 0 }, // Ziffer 1
			{ 1, 1, 0, 1, 1, 0, 1 }, // Ziffer 2
			{ 1, 1, 1, 1, 0, 0, 1 }, // Ziffer 3
			{ 0, 1, 1, 0, 0, 1, 1 }, // Ziffer 4
			{ 1, 0, 1, 1, 0, 1, 1 }, // Ziffer 5
			{ 1, 0, 1, 1, 1, 1, 1 }, // Ziffer 6
			{ 1, 1, 1, 0, 0, 0, 0 }, // Ziffer 7
			{ 1, 1, 1, 1, 1, 1, 1 }, // Ziffer 8
			{ 1, 1, 1, 1, 0, 1, 1 }, // Ziffer 9
			{ 0, 0, 0, 0, 0, 0, 0 } // Ziffer -
											};

	public Segment7() {
		this(-1);
	}

	public Segment7(int digit) {
		super();
		this.digit = digit;
		this.hasfocus = false;
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
		enableEvents(AWTEvent.FOCUS_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.KEY_EVENT_MASK);
	}

	public Dimension getPreferredSize() {
		return new Dimension(6 * 11, 5 * 20);
	}

	public Dimension getMinimumSize() {
		return new Dimension(6 * 11, 5 * 20);
	}

	public boolean isFocusTraversable() {
		return true;
	}

	public void paint(Graphics g) {

		Graphics2D g2d =(Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		Color background = new Color(0, 0, 0);
		Color foreground = new Color(255, 255, 0);
		Color activatedColor = new Color(0, 255, 0);
		Color deactivatedColor = new Color(30,30,30);
		// dx und dy berechnen
		int dx = (getSize().width-11) / 11;
		int dy = getSize().height / 20;
		// Hintergrund
		g2d.setColor(background);
		g2d.fillRect(0, 0, getSize().width, getSize().height);

		AffineTransform shear = AffineTransform.getShearInstance(-0.1, 0);
		g2d.transform(shear);
		AffineTransform translate = AffineTransform.getTranslateInstance(11, 0);
		g2d.transform(translate);
		
		
		// Segmente
		if (hasfocus) {
			g2d.setColor(activatedColor);
		} else {
			g2d.setColor(foreground);
		}
		int tmpDigit;
		if (digit == -1) {
			tmpDigit = 10;
		} else {
			tmpDigit = digit;
		}
		for (int i = 0; i < 7; ++i) { // alle Segmente
			if (digits[tmpDigit][i] == 1) {
				g2d.setColor(activatedColor);
			} else {
				g2d.setColor(deactivatedColor);
			}
			Polygon poly = new Polygon();
			for (int j = 0; j < 6; ++j) { // alle Eckpunkte
				poly.addPoint(dx * (polysx[i][j] + 1), dy * (polysy[i][j] + 1));
			}
			g2d.fillPolygon(poly);
		}
		// Trennlinien
		g2d.setColor(background);
		g2d.drawLine(0, 0, dx * 10, dy * 10);
		g2d.drawLine(0, 8 * dy, 10 * dx, 18 * dy);
		g2d.drawLine(0, 12 * dy, 12 * dx, 0);
		g2d.drawLine(0, 20 * dy, 10 * dx, 10 * dy);

		g2d.setColor(activatedColor);
		if (displayPeriod) {
			Polygon poly = new Polygon();
			for (int i = 0; i < periodx.length; ++i) {
				poly.addPoint(dx * periodx[i], dy * periody[i]);
			}
			g2d.fillPolygon(poly);
		}

	}

	public int getValue() {
		return digit;
	}

	public void setValue(int value) {
		if (value == -1)
			digit = -1;
		else
			digit = value % 10;
	}

	public void setDisplayPeriod(boolean displayPeriod) {
		this.displayPeriod = displayPeriod;
	}
}
