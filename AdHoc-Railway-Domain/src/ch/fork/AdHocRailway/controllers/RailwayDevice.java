package ch.fork.AdHocRailway.controllers;

public enum RailwayDevice {
	SRCP("srcp"), ADHOC_BRAIN("adhoc-brain");

	private final String key;

	private RailwayDevice(final String key) {
		this.key = key;

	}

	public static RailwayDevice fromString(final String railwayDevice) {
		for (final RailwayDevice device : values()) {
			if (device.key.equalsIgnoreCase(railwayDevice)) {
				return device;
			}
		}
		return null;
	}
}