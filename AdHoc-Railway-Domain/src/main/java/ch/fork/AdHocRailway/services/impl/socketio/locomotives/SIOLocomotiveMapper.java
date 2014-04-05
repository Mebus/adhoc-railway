package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class SIOLocomotiveMapper {

    public static Map<String, String> locomotiveIdMap = Maps.newHashMap();
    public static Map<String, String> locomotiveGroupIdMap = Maps.newHashMap();

    public static LocomotiveGroup mapLocomotiveGroupFromJSON(
            final JSONObject locomotiveGroupJSON) throws JSONException {
        final String sioId = locomotiveGroupJSON.getString("id");
        final String id = sioId;
        locomotiveGroupIdMap.put(id, sioId);
        final LocomotiveGroup locomotiveGroup = new LocomotiveGroup(id, "temp");

        mergeLocomotiveGroupBaseInfo(locomotiveGroup, locomotiveGroupJSON);

        if (locomotiveGroupJSON.has("locomotives")) {
            final JSONArray locomotivesJSON = locomotiveGroupJSON
                    .getJSONArray("locomotives");
            for (int i = 0; i < locomotivesJSON.length(); i++) {
                final JSONObject locomotiveJSON = locomotivesJSON.getJSONObject(i);
                final Locomotive locomotive = mapLocomotiveFromJSON(locomotiveJSON);
                locomotive.setGroup(locomotiveGroup);
                locomotiveGroup.addLocomotive(locomotive);
            }
        }
        return locomotiveGroup;
    }

    public static Locomotive mapLocomotiveFromJSON(
            final JSONObject locomotiveJSON) throws JSONException {
        final Locomotive locomotive = new Locomotive();
        final String sioId = locomotiveJSON.getString("id");
        final String id = sioId;
        locomotiveIdMap.put(id, sioId);
        locomotive.setId(id);
        mergeLocomotiveBaseInfo(locomotiveJSON, locomotive);

        locomotive.init();

        return locomotive;
    }

    public static void mergeLocomotiveBaseInfo(final JSONObject locomotiveJSON,
                                               final Locomotive locomotive) throws JSONException {
        locomotive.setName(locomotiveJSON.getString("name"));
        locomotive.setDesc(locomotiveJSON.optString("description", ""));

        final String imageString = locomotiveJSON.optString("image", "");
        if (StringUtils.isNotBlank(imageString)
                && !StringUtils.equalsIgnoreCase(imageString, "null")) {
            locomotive.setImage(imageString);
        } else {
            locomotive.setImage("");
        }

        final String imageBase64String = locomotiveJSON.optString("imageBase64", "");
        if (StringUtils.isNotBlank(imageBase64String)
                && !StringUtils.equalsIgnoreCase(imageBase64String, "null")) {
            locomotive.setImageBase64(imageBase64String);
        } else {
            locomotive.setImageBase64("");
        }
        locomotive.setBus(locomotiveJSON.getInt("bus"));
        locomotive.setAddress1(locomotiveJSON.getInt("address1"));
        locomotive.setAddress2(locomotiveJSON.optInt("address2", 0));

        locomotive.setType(LocomotiveType.fromString(locomotiveJSON
                .getString("type")));

        if (locomotiveJSON.has("functions")) {
            final JSONArray functionsJSON = locomotiveJSON
                    .getJSONArray("functions");

            for (int i = 0; i < functionsJSON.length(); i++) {
                final JSONObject functionJSON = functionsJSON.getJSONObject(i);

                if (functionJSON.has("number")) {
                    final LocomotiveFunction function = new LocomotiveFunction(
                            functionJSON.getInt("number"),
                            functionJSON.getString("description"),
                            functionJSON.getBoolean("emergencyBrakeFunction"),
                            functionJSON.optInt("deactivationDelay", -1));

                    locomotive.addLocomotiveFunction(function);
                }
            }

        }

    }

    public static void mergeLocomotiveGroupBaseInfo(
            final LocomotiveGroup locomotiveGroup,
            final JSONObject locomotiveGroupJSON) throws JSONException {

        locomotiveGroup.setName(locomotiveGroupJSON.getString("name"));

    }

    public static JSONObject mapLocomotiveGroupToJSON(
            final LocomotiveGroup group) throws JSONException {
        final JSONObject locomotiveGroupJSON = new JSONObject();
        locomotiveGroupJSON.put("id", locomotiveGroupIdMap.get(group.getId()));
        locomotiveGroupJSON.put("name", group.getName());
        return locomotiveGroupJSON;
    }

    public static JSONObject mapLocomotiveToJSON(final Locomotive locomotive)
            throws JSONException {
        final JSONObject locomotiveJSON = new JSONObject();
        locomotiveJSON.put("id", locomotiveIdMap.get(locomotive.getId()));
        locomotiveJSON.put("name", locomotive.getName());
        locomotiveJSON.put("description", locomotive.getDesc());
        locomotiveJSON.put("bus", locomotive.getBus());
        locomotiveJSON.put("address1", locomotive.getAddress1());
        locomotiveJSON.put("address2", locomotive.getAddress2());
        locomotiveJSON.put("image", locomotive.getImage());
        locomotiveJSON.put("imageBase64", locomotive.getImageBase64());
        locomotiveJSON.put("type", locomotive.getType().getId().toLowerCase());

        final org.json.JSONArray functionsJSON = new org.json.JSONArray();
        for (final LocomotiveFunction function : locomotive.getFunctions()) {
            final JSONObject functionJSON = new JSONObject();
            functionJSON.put("number", function.getNumber());
            functionJSON.put("description", function.getDescription());
            functionJSON.put("emergencyBrakeFunction",
                    function.isEmergencyBrakeFunction());
            functionJSON.put("deactivationDelay",
                    function.getDeactivationDelay());
            functionsJSON.put(functionJSON);
        }
        locomotiveJSON.put("functions", functionsJSON);

        locomotiveJSON.put("group",
                locomotiveGroupIdMap.get(locomotive.getGroup().getId()));

        return locomotiveJSON;

    }

}
