package ch.fork.AdHocRailway.utils;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.model.turnouts.*;

import java.util.HashSet;
import java.util.SortedSet;

public class DataImporter {

    public void importLocomotives(
            final LocomotiveManager locomotiveManager,
            final SortedSet<LocomotiveGroup> groups) {

        locomotiveManager.clearToService();
        HashSet<LocomotiveGroup> copyLocomotiveGroup = new HashSet<>(groups);
        locomotiveManager.addLocomotiveGroups(groups);

        for (final LocomotiveGroup group : copyLocomotiveGroup) {
            for (final Locomotive locomotive : group.getLocomotives()) {
                locomotiveManager.addLocomotiveToGroup(locomotive, group);
            }
        }
    }

    public void importTurnouts(TurnoutManager turnoutManager, SortedSet<TurnoutGroup> turnoutGroups) {
        turnoutManager.clearToService();

        HashSet<TurnoutGroup> copyTurnoutGroups = new HashSet<>(turnoutGroups);
        for (final TurnoutGroup group : turnoutGroups) {
            turnoutManager.addTurnoutGroup(group);
        }

        for (final TurnoutGroup group : copyTurnoutGroups) {
            for (Turnout turnout : new HashSet<>(group.getTurnouts())) {
                turnoutManager.addTurnoutToGroup(turnout, group);
            }
        }
    }

    public void importRoutes(RouteManager routeManager, SortedSet<RouteGroup> routeGroups) {
        routeManager.clearToService();
        HashSet<RouteGroup> copyRouteGroups = new HashSet<>(routeGroups);
        for (final RouteGroup group : copyRouteGroups) {
            routeManager.addRouteGroup(group);
        }

        for (final RouteGroup group : copyRouteGroups) {

            for (Route route : new HashSet<>(group.getRoutes())) {
                routeManager.addRouteToGroup(route, group);
                for (RouteItem routeItem :  new HashSet<>(route.getRoutedTurnouts())) {
                    if (routeItem.getTurnout() != null) {
                        routeManager.addRouteItemToGroup(routeItem, route);
                    }
                }
            }
        }
    }
}
