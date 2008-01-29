package ch.fork.AdHocRailway.ui.routes.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;

public class RoutedSwitchesTableModel extends AbstractTableModel {

	private String[] columnNames = { "Switch-Number", "Routed-State" };

	private Set<RouteItem> routeItems;

	public RoutedSwitchesTableModel() {
	}

	public int getRowCount() {
		if (routeItems == null) {
			return 0;
		}
		return routeItems.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
        List<RouteItem> routeItemsArrayList = new ArrayList<RouteItem>(routeItems);
		RouteItem actualRouteItem = routeItemsArrayList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return actualRouteItem.getTurnout();
		case 1:
			return actualRouteItem.getRoutedStateEnum();
		}
		return null;
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex) {
        List<RouteItem> routeItemsArrayList = new ArrayList<RouteItem>(routeItems);
		RouteItem actualRouteItem = routeItemsArrayList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return;
		case 1:
			TurnoutState state = (TurnoutState) value;
			actualRouteItem.setRoutedStateEnum(state);
		}
		return;
	}

	public void setRoute(Route route) {
		if(route.getRouteItems() != null) 
		this.routeItems = route.getRouteItems();
		fireTableDataChanged();
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public boolean isCellEditable(int row, int column) {
		switch (column) {
		case 0:
			return false;
		case 1:
			return true;
		}
		return false;
	}
}
