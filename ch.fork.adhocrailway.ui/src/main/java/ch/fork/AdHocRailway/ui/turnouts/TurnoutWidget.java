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

import ch.fork.AdHocRailway.controllers.TurnoutChangeListener;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfig;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutHelper;
import ch.fork.AdHocRailway.ui.utils.UIConstants;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TurnoutWidget extends JPanel implements TurnoutChangeListener {

    private final boolean testMode;
    private final boolean forHistory;
    private final TurnoutManager turnoutManager;
    private final TurnoutContext ctx;
    private Turnout turnout;
    private JLabel numberLabel;
    private TurnoutCanvas turnoutCanvas;
    private TurnoutState actualTurnoutState = TurnoutState.UNDEF;
    private boolean connectedToRailway;
    private JPanel statePanel;

    public TurnoutWidget(final TurnoutContext ctx, final Turnout turnout,
                         final boolean forHistory) {
        this(ctx, turnout, forHistory, false);
    }

    public TurnoutWidget(final TurnoutContext ctx, final Turnout turnout,
                         final boolean forHistory, final boolean testMode) {
        this.ctx = ctx;
        this.turnout = turnout;
        this.forHistory = forHistory;
        this.testMode = testMode;

        turnoutManager = ctx.getTurnoutManager();

        ctx.getMainBus().register(this);

        initGUI();
        updateTurnout();
        TurnoutHelper.validateTurnout(turnoutManager, turnout, this);
        ctx.getTurnoutControl().addTurnoutChangeListener(turnout, this);
        connectedToRailway = false;
    }

    @Subscribe
    public void connectedToRailwayDevice(final ConnectedToRailwayEvent event) {
        if (event.isConnected()) {
            connectedToRailway = true;
            ctx.getTurnoutControl().addTurnoutChangeListener(turnout, this);
        } else {
            connectedToRailway = false;
            ctx.getTurnoutControl().removeTurnoutChangeListener(turnout, this);
        }
    }


    private void initGUI() {
        turnoutCanvas = new TurnoutCanvas(turnout);
        turnoutCanvas.addMouseListener(new MouseAction());
        addMouseListener(new MouseAction());

        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        numberLabel = new JLabel();
        numberLabel.setFont(new Font("Dialog", Font.BOLD, 25));
        statePanel = new JPanel();

        setLayout(new MigLayout());

        if (forHistory) {
            add(numberLabel);
            add(turnoutCanvas);
        } else {
            add(numberLabel, "");
            add(statePanel, "wrap, w 20!, h 7!, align right");
            add(turnoutCanvas, "span 2");
        }

    }

    public void updateTurnout() {
        numberLabel.setText(Integer.toString(turnout.getNumber()));

        final String turnoutDescription = TurnoutHelper
                .getTurnoutDescription(turnout);
        setToolTipText(turnoutDescription);
        turnoutCanvas.setToolTipText(turnoutDescription);
    }

    public Turnout getTurnout() {
        return turnout;
    }

    public void setTurnout(final Turnout turnout) {
        this.turnout = turnout;
        updateTurnout();
    }

    @Override
    public void turnoutChanged(final Turnout changedTurnout) {

        if (turnout.equals(changedTurnout)) {
            actualTurnoutState = changedTurnout.getActualState();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    numberLabel.setText(Integer.toString(turnout.getNumber()));
                    if (actualTurnoutState == null) {
                        actualTurnoutState = TurnoutState.UNDEF;
                    }
                    turnoutCanvas.setTurnoutState(actualTurnoutState);
                    switch (actualTurnoutState) {
                        case LEFT:
                        case RIGHT:
                            if (turnout.getDefaultState().equals(
                                    TurnoutState.STRAIGHT)) {
                                statePanel.setBackground(UIConstants.STATE_RED);
                            } else {
                                statePanel.setBackground(UIConstants.STATE_GREEN);
                            }

                            break;
                        case STRAIGHT:
                            if (turnout.getDefaultState().equals(
                                    TurnoutState.STRAIGHT)) {
                                statePanel.setBackground(UIConstants.STATE_GREEN);
                            } else {
                                statePanel.setBackground(UIConstants.STATE_RED);
                            }

                            break;
                        case UNDEF:
                        default:
                            statePanel.setBackground(Color.GRAY);
                            break;

                    }
                    TurnoutWidget.this.revalidate();
                    TurnoutWidget.this.repaint();
                }
            });
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            setBackground(new Color(255, 177, 177));
        }
        connectedToRailway = enabled;
        turnoutCanvas.setTurnoutState(TurnoutState.UNDEF);
    }

    public void revalidateTurnout() {
        TurnoutHelper.validateTurnout(turnoutManager, turnout, this);
    }

    private class MouseAction extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent e) {

            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                handleLeftClick();
            } else if (e.getClickCount() == 1
                    && e.getButton() == MouseEvent.BUTTON3) {

                handleRightClick();
            }
        }

        private void handleRightClick() {
            if (ctx.isEditingMode()) {
                displaySwitchConfig();
            }
        }

        private void handleLeftClick() {
            if (!connectedToRailway) {
                return;
            }
            final TurnoutController turnoutControl = ctx
                    .getTurnoutControl();
            if (!testMode) {
                turnoutControl.toggle(turnout);
            } else {
                turnoutControl.toggleTest(turnout);
            }
        }

        private void displaySwitchConfig() {
            if (testMode) {
                return;
            }

            final TurnoutController turnoutControl = ctx.getTurnoutControl();
            turnoutControl
                    .removeGeneralTurnoutChangeListener(TurnoutWidget.this);
            new TurnoutConfig(ctx.getMainFrame(), ctx, turnout,
                    turnout.getTurnoutGroup(), false);
            TurnoutHelper.validateTurnout(turnoutManager, turnout,
                    TurnoutWidget.this);
            turnoutControl.addGeneralTurnoutChangeListener(TurnoutWidget.this);

            turnoutChanged(turnout);
        }
    }
}