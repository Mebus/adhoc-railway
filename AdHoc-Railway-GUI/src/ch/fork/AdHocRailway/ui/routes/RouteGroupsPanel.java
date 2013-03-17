package ch.fork.AdHocRailway.ui.routes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;
import ch.fork.AdHocRailway.domain.routes.RouteManagerListener;
import ch.fork.AdHocRailway.ui.AdHocRailway;

public class RouteGroupsPanel extends JTabbedPane implements
		RouteManagerListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8209638004262038025L;

	private final Map<Integer, RouteGroup> indexToRouteGroup = new HashMap<Integer, RouteGroup>();

	private final Map<RouteGroup, RouteGroupTab> routeGroupToRouteGroupTab = new HashMap<RouteGroup, RouteGroupTab>();

	private final RouteManager routePersistence = AdHocRailway.getInstance()
			.getRoutePersistence();

	public RouteGroupsPanel(final int tabPlacement) {
		super(tabPlacement);
		routePersistence.addRouteManagerListener(this);

	}

	private void updateRoutes(final List<RouteGroup> routeGroups) {
		indexToRouteGroup.clear();
		removeAll();
		routeGroupToRouteGroupTab.clear();

		int i = 1;
		final RouteControlIface routeControl = AdHocRailway.getInstance()
				.getRouteControl();

		routeControl.removeAllRouteChangeListeners();

		for (final RouteGroup routeGroup : routeGroups) {
			indexToRouteGroup.put(i - 1, routeGroup);
			addRouteGroup(i, routeGroup);
			i++;
		}
	}

	public void addRouteGroup(final int groupNumber, final RouteGroup routeGroup) {
		final RouteGroupTab routeGroupTab = new RouteGroupTab(routeGroup);

		add(routeGroupTab, "F" + groupNumber + ": " + routeGroup.getName());
		routeGroupToRouteGroupTab.put(routeGroup, routeGroupTab);

	}

	@Override
	public void routesUpdated(final List<RouteGroup> routeGroups) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				updateRoutes(routeGroups);
			}
		});
		revalidate();
		repaint();
	}

	@Override
	public void routeUpdated(final Route route) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(route.getRouteGroup());
				routeGroupTab.updateRoute(route);
				revalidate();
				repaint();

			}
		});

	}

	@Override
	public void routeRemoved(final Route route) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(route.getRouteGroup());
				routeGroupTab.removeRoute(route);

				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void routeAdded(final Route route) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(route.getRouteGroup());
				routeGroupTab.addRoute(route);
				revalidate();
				repaint();

			}
		});

	}

	@Override
	public void routeGroupAdded(final RouteGroup group) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				addRouteGroup(-1, group);
				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void routeGroupRemoved(final RouteGroup group) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(group);
				remove(routeGroupTab);
				revalidate();
				repaint();
			}
		});

	}

	@Override
	public void routeGroupUpdated(final RouteGroup group) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(group);
				routeGroupTab.updateRouteGroup(group);
				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void failure(final RouteManagerException arg0) {

	}
}
