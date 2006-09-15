
package ch.fork.AdHocRailway.domain.routes;

import java.util.ArrayList;
import java.util.List;


public class Route implements Comparable {

    private List<RouteItem> routeItems;
    private String          name;
    private boolean         enabled;


    public Route(String name) {
        this.name = name;
        routeItems = new ArrayList<RouteItem>();
    }

    public void addRouteItem(RouteItem item) {
        routeItems.add(item);
    }

    public void removeRouteItem(RouteItem item) {
        routeItems.remove(item);
    }

    public List<RouteItem> getRouteItems() {
        return routeItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (o instanceof Route) {
            Route route = (Route) o;
            return route.getName().equals(name);
        }
        return false;
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String toString() {
        /*
        String output = name + ": {";
        for (RouteItem r : routeItems) {
            output += " " + r + " ,";
        }
        return output += "}";
        */
        return name;
    }

    public Object clone() {
        Route newRoute = new Route(name);
        for (RouteItem origItem : routeItems) {
            newRoute.addRouteItem((RouteItem) origItem.clone());
        }
        return newRoute;
    }

	public int compareTo(Object o) {
		if (o instanceof Route) {
			Route r = (Route) o;
			return name.compareTo(r.getName());
		}
		return -1;
	}
}
