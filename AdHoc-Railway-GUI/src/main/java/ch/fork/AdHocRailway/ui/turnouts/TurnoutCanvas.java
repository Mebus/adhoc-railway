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

package ch.fork.AdHocRailway.ui.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.ui.tools.ImageTools;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static ch.fork.AdHocRailway.ui.tools.ImageTools.createImageIconFromCustom;

public class TurnoutCanvas extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -7177529935222900726L;
    protected Turnout turnout;
    protected TurnoutState turnoutState = TurnoutState.UNDEF;

    public TurnoutCanvas(final Turnout turnout) {
        this.turnout = turnout;
    }

    @Override
    public void paintComponent(final Graphics g) {
        if (turnout.isDoubleCross()) {
            paintDoubleCross(g);
        } else if (turnout.isDefaultLeft()) {
            paintDefaultLeft(g);
        } else if (turnout.isDefaultRight()) {
            paintDefaultRight(g);
        } else if (turnout.isThreeWay()) {
            paintThreeway(g);
        } else if (turnout.isCutter()) {
            paintCutter(g);
        }
    }

    protected void rotate(final Graphics g, final BufferedImage img) {
        final Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = null;
        switch (turnout.getOrientation()) {
            case NORTH:
                at = AffineTransform.getRotateInstance(Math.PI / 2 * 3,
                        (56 + 1) / 2, (56 + 1) / 2);
                at.concatenate(AffineTransform.getTranslateInstance(0, 10));
                break;
            case EAST:
                at = AffineTransform.getRotateInstance(0, 0, 0);
                at.concatenate(AffineTransform.getTranslateInstance(0, 14));
                break;
            case SOUTH:
                at = AffineTransform.getRotateInstance(Math.PI / 2, (56 + 1) / 2,
                        (56 + 1) / 2);
                at.concatenate(AffineTransform.getTranslateInstance(0, 10));
                break;
            case WEST:
                at = AffineTransform.getRotateInstance(Math.PI, (56 + 1) / 2,
                        (56 + 1) / 2);
                at.concatenate(AffineTransform.getTranslateInstance(0, 14));
                break;
        }
        g2.drawImage(img, at, this);
    }

    private void paintDefaultLeft(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 35,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(createImageIconFromCustom("canvas/default_switch_left.png")
                .getImage(), 0, 0, this);
        switch (turnoutState) {
            case STRAIGHT:
                g3.drawImage(
                        createImageIconFromCustom("canvas/LED_middle_yellow.png")
                                .getImage(), 28, 0, this);
                g3.drawImage(createImageIconFromCustom("canvas/LED_up_white.png")
                        .getImage(), 28, 0, this);
                break;
            case LEFT:
            case RIGHT:
                g3.drawImage(createImageIconFromCustom("canvas/LED_up_yellow.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                break;
            case UNDEF:
                g3.drawImage(createImageIconFromCustom("canvas/LED_up_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                break;
        }
        g3.drawImage(createImageIconFromCustom("canvas/LED_middle_white.png")
                .getImage(), 0, 0, this);
        rotate(g, img);
    }

    private void paintDefaultRight(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 35,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(
                createImageIconFromCustom("canvas/default_switch_right.png")
                        .getImage(), 0, 0, this);
        switch (turnoutState) {
            case STRAIGHT:
                g3.drawImage(
                        createImageIconFromCustom("canvas/LED_middle_yellow.png")
                                .getImage(), 28, 0, this);
                g3.drawImage(createImageIconFromCustom("canvas/LED_down_white.png")
                        .getImage(), 28, 0, this);
                break;
            case LEFT:
            case RIGHT:
                g3.drawImage(
                        createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                g3.drawImage(createImageIconFromCustom("canvas/LED_down_yellow.png")
                        .getImage(), 28, 0, this);
                break;
            case UNDEF:
                g3.drawImage(createImageIconFromCustom("canvas/LED_down_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                break;
        }
        g3.drawImage(createImageIconFromCustom("canvas/LED_middle_white.png")
                .getImage(), 0, 0, this);
        rotate(g, img);
    }

    private void paintCutter(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 35,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(
                createImageIconFromCustom("cutter.png").getImage(), 0, 0,
                this);

        rotate(g, img);
    }

    private void paintDoubleCross(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 35,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(ImageTools
                .createImageIconFromCustom("canvas/double_cross_switch.png")
                .getImage(), 0, 0, this);
        switch (turnoutState) {
            case STRAIGHT:
                g3.drawImage(ImageTools
                        .createImageIconFromCustom("canvas/LED_up_yellow.png")
                        .getImage(), 0, 17, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 0, 0, this);
                g3.drawImage(ImageTools
                        .createImageIconFromCustom("canvas/LED_up_yellow.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                break;
            case RIGHT:
            case LEFT:
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom("canvas/LED_middle_yellow.png")
                                .getImage(), 0, 0, this);
                g3.drawImage(ImageTools
                        .createImageIconFromCustom("canvas/LED_up_white.png")
                        .getImage(), 0, 17, this);
                g3.drawImage(ImageTools
                        .createImageIconFromCustom("canvas/LED_up_yellow.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                break;
            case UNDEF:
                g3.drawImage(ImageTools
                        .createImageIconFromCustom("canvas/LED_up_white.png")
                        .getImage(), 0, 17, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 0, 0, this);
                g3.drawImage(ImageTools
                        .createImageIconFromCustom("canvas/LED_up_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                break;
        }
        rotate(g, img);
    }

    private void paintThreeway(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 35,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(ImageTools.createImageIconFromCustom("canvas/three_way_switch.png")
                .getImage(), 0, 0, this);
        switch (turnoutState) {
            case LEFT:
                g3.drawImage(ImageTools.createImageIconFromCustom("canvas/LED_up_yellow.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                g3.drawImage(ImageTools.createImageIconFromCustom("canvas/LED_down_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 0, 0, this);
                break;
            case STRAIGHT:
                g3.drawImage(ImageTools.createImageIconFromCustom("canvas/LED_up_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom("canvas/LED_middle_yellow.png")
                                .getImage(), 28, 0, this);
                g3.drawImage(ImageTools.createImageIconFromCustom("canvas/LED_down_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 0, 0, this);
                break;
            case RIGHT:
                g3.drawImage(ImageTools.createImageIconFromCustom("canvas/LED_up_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                g3.drawImage(ImageTools.createImageIconFromCustom("canvas/LED_down_yellow.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 0, 0, this);
                break;
            case UNDEF:
                g3.drawImage(ImageTools.createImageIconFromCustom("canvas/LED_up_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 28, 0, this);
                g3.drawImage(ImageTools.createImageIconFromCustom("canvas/LED_down_white.png")
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom("canvas/LED_middle_white.png")
                                .getImage(), 0, 0, this);
        }
        rotate(g, img);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(56, 56);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public boolean isFocusTraversable() {
        return true;
    }

    public void setTurnoutState(final TurnoutState turnoutState) {
        this.turnoutState = turnoutState;
    }
}
