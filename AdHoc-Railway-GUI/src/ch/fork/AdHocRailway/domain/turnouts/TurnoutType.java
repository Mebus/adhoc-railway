package ch.fork.AdHocRailway.domain.turnouts;

public enum TurnoutType {
	DEFAULT_LEFT, DEFAULT_RIGHT, DOUBLECROSS, THREEWAY, CUTTER;

	public static TurnoutType fromString(final String string) {
		for (final TurnoutType ts : values()) {
			if (ts.name().equalsIgnoreCase(string)) {
				return ts;
			}
		}
		return null;
	}
}
