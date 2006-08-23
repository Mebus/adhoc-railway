/*------------------------------------------------------------------------
 * 
 * <./ui/ImageTools.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:28 BST 2006
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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

public class ImageTools {

    public static ImageIcon createStraightState(ImageObserver obs, Class comp) {
        BufferedImage img = new BufferedImage(120, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        // DefaultSwitch
        g.drawImage(createImageIcon("icons/default_switch.png", "", comp)
            .getImage(), 0, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_yellow.png", "", comp)
            .getImage(), 28, 0, obs);
        g.drawImage(createImageIcon("icons/LED_up_white.png", "", comp)
            .getImage(), 28, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_white.png", "", comp)
            .getImage(), 0, 0, obs);
        // DoubleCrossSwitch
        g.drawImage(createImageIcon("icons/double_cross_switch.png", "", comp)
            .getImage(), 64, 0, obs);
        g.drawImage(createImageIcon("icons/LED_up_yellow.png", "", comp)
            .getImage(), 64, 17, obs);
        g.drawImage(createImageIcon("icons/LED_middle_white.png", "", comp)
            .getImage(), 64, 0, obs);
        g.drawImage(createImageIcon("icons/LED_up_yellow.png", "", comp)
            .getImage(), 92, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_white.png", "", comp)
            .getImage(), 92, 0, obs);
        return new ImageIcon(img);
    }

    public static ImageIcon createCurvedState(ImageObserver obs, Class comp) {
        BufferedImage img = new BufferedImage(120, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        // DefaultSwitch
        g.drawImage(createImageIcon("icons/default_switch.png", "", comp)
            .getImage(), 0, 0, obs);
        g.drawImage(createImageIcon("icons/LED_up_yellow.png", "", comp)
            .getImage(), 28, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_white.png", "", comp)
            .getImage(), 28, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_white.png", "", comp)
            .getImage(), 0, 0, obs);
        // DoubleCrossSwitch
        g.drawImage(createImageIcon("icons/double_cross_switch.png", "", comp)
            .getImage(), 64, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_yellow.png", "", comp)
            .getImage(), 64, 0, obs);
        g.drawImage(createImageIcon("icons/LED_up_white.png", "", comp)
            .getImage(), 64, 17, obs);
        g.drawImage(createImageIcon("icons/LED_up_yellow.png", "", comp)
            .getImage(), 92, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_white.png", "", comp)
            .getImage(), 92, 0, obs);
        return new ImageIcon(img);
    }

    public static ImageIcon createEastOrientation(ImageObserver obs, Class comp) {
        BufferedImage img = new BufferedImage(120, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        // DefaultSwitch
        g.drawImage(createImageIcon("icons/default_switch.png", "", comp)
            .getImage(), 0, 0, obs);
        // DoubleCrossSwitch
        g.drawImage(createImageIcon("icons/double_cross_switch.png", "", comp)
            .getImage(), 64, 0, obs);
        return new ImageIcon(img);
    }

    public static ImageIcon createWestOrientation(ImageObserver obs, Class comp) {
        BufferedImage img = new BufferedImage(120, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        AffineTransform rotateSwitch = AffineTransform.getRotateInstance(
            Math.PI, 56 / 2, 35 / 2);
        // DefaultSwitch
        g.drawImage(createImageIcon("icons/default_switch.png", "", comp)
            .getImage(), rotateSwitch, obs);
        // DoubleCrossSwitch
        g.drawImage(createImageIcon("icons/double_cross_switch.png", "", comp)
            .getImage(), 64, 0, obs);
        return new ImageIcon(img);
    }

    public static ImageIcon createNorthOrientation(ImageObserver obs, Class comp) {
        BufferedImage img = new BufferedImage(120, 56,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        AffineTransform rotateSwitch = AffineTransform.getRotateInstance(
            Math.PI / 2 * 3, 56 / 2, 35 / 2);
        // DefaultSwitch
        g.drawImage(createImageIcon("icons/default_switch.png", "", comp)
            .getImage(), rotateSwitch, obs);
        // DoubleCrossSwitch
        g.drawImage(createImageIcon("icons/double_cross_switch.png", "", comp)
            .getImage(), 64, 0, obs);
        return new ImageIcon(img);
    }

    public static ImageIcon createSourthOrientation(ImageObserver obs,
        Class comp) {
        BufferedImage img = new BufferedImage(120, 60,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        AffineTransform rotateSwitch = AffineTransform.getRotateInstance(
            Math.PI / 2 * 3, 56 / 2, 35 / 2);
        // DefaultSwitch
        g.drawImage(createImageIcon("icons/default_switch.png", "", comp)
            .getImage(), rotateSwitch, obs);
        // DoubleCrossSwitch
        g.drawImage(createImageIcon("icons/double_cross_switch.png", "", comp)
            .getImage(), 64, 0, obs);
        return new ImageIcon(img);
    }

    public static ImageIcon createStraightSwitch(ImageObserver obs, Class comp) {
        BufferedImage img = new BufferedImage(56, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        // DefaultSwitch
        g.drawImage(createImageIcon("icons/default_switch.png", "", comp)
            .getImage(), 0, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_yellow.png", "", comp)
            .getImage(), 28, 0, obs);
        g.drawImage(createImageIcon("icons/LED_up_white.png", "", comp)
            .getImage(), 28, 0, obs);
        g.drawImage(createImageIcon("icons/LED_middle_white.png", "", comp)
            .getImage(), 0, 0, obs);
        return new ImageIcon(img);
    }

    public static ImageIcon createImageIcon(String path, String description,
        Object o) {
        return createImageIcon(path, description, o.getClass());
    }

    public static ImageIcon createImageIcon(String path, String description,
        Class o) {
        java.net.URL imgURL = o.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
