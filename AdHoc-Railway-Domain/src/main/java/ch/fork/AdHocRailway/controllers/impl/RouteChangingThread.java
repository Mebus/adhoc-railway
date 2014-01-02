package ch.fork.AdHocRailway.controllers.impl;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutChangeListener;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;

/**
 * Created by fork on 1/2/14.
 */
public class RouteChangingThread implements Runnable, TurnoutChangeListener {

    private TurnoutController turnoutControl;
    private final Route route;
    private final boolean enable;
    private long routingDelay;
    private RouteChangingListener listener;

    public RouteChangingThread(TurnoutController turnoutControl, final Route route, final boolean enable, long routingDelay, RouteChangingListener listener) {
        this.turnoutControl = turnoutControl;
        this.route = route;
        this.enable = enable;
        this.routingDelay = routingDelay;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            route.setRouting(true);
            turnoutControl.addGeneralTurnoutChangeListener(this);
            for (final RouteItem routeItem : route.getRouteItems()) {
                final Turnout turnout = routeItem.getTurnout();
                if (enable) {
                    switch (routeItem.getRoutedState()) {
                        case LEFT:
                            turnoutControl.setCurvedLeft(turnout);

                            break;
                        case RIGHT:
                            turnoutControl.setCurvedRight(turnout);
                            break;
                        case STRAIGHT:
                            turnoutControl.setStraight(turnout);
                            break;
                        case UNDEF:
                        default:
                            throw new IllegalStateException(
                                    "routed state must be one of LEFT, RIGHT or STRAIGHT");

                    }
                } else {
                    turnoutControl.setDefaultState(turnout);
                }
                Thread.sleep(routingDelay);

            }
            turnoutControl.removeGeneralTurnoutChangeListener(this);
            route.setEnabled(enable);
            route.setRouting(false);
            listener.informRouteChanged(route);
        } catch (final TurnoutException e) {
            e.printStackTrace();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } finally {
            route.setRouting(false);
        }
    }

    @Override
    public void turnoutChanged(final Turnout changedTurnout) {
        if (enable) {
            listener.informNextTurnoutRouted(route);
        } else {
            listener.informNextTurnoutDerouted(route);
        }
    }

    public interface RouteChangingListener {
        void informNextTurnoutRouted(Route route);

        void informNextTurnoutDerouted(Route route);

        void informRouteChanged(Route route);
    }
}
