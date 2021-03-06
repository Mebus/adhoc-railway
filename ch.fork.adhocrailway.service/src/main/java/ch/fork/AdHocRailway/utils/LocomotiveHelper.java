package ch.fork.AdHocRailway.utils;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveFunction;
import org.apache.commons.lang3.StringUtils;

public class LocomotiveHelper {

    public static void toggleDirection(final Locomotive locomotive) {
        if (locomotive.getCurrentDirection() == LocomotiveDirection.FORWARD) {
            locomotive.setCurrentDirection(LocomotiveDirection.REVERSE);
        } else {
            locomotive.setCurrentDirection(LocomotiveDirection.FORWARD);
        }
    }

    public static String getLocomotiveDescription(final Locomotive locomotive) {
        if (locomotive == null) {
            return "Please choose a locomotive...";
        }
        final StringBuilder description = new StringBuilder();
        description.append("<html>");
        description.append("<h3>" + locomotive.getName() + "</h3>");
        if (StringUtils.isBlank(locomotive.getDesc())) {
            description.append("no description");
        } else {
            description.append(locomotive.getDesc());
        }
        description.append("<br/><br/>");
        description.append("Type: " + locomotive.getType());
        description.append("<br/><hr/>");
        description.append("<table>");
        for (final LocomotiveFunction f : locomotive.getFunctions()) {
            description.append("<tr>");
            description.append("<td>" + f.getShortDescription() + "</td>");
            description.append("<td>" + f.getDescription() + "</td>");
            description.append("</tr>");
        }
        description.append("</table>");
        description.append("</html>");
        return description.toString();
    }
}
