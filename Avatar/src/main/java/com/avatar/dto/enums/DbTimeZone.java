package com.avatar.dto.enums;


public enum DbTimeZone {
	US_ALASKA("US/Alaska"), //
	US_PST("US/Pacific"), //
	US_CENTRAL("US/Central"), //
	US_EASTERN("US/Eastern"), //
	US_MOUNTAIN("US/Mountain"), //
	US_SAMOA("US/Samoa");

	public static DbTimeZone convert(final String timezone) {
		DbTimeZone retVal = US_PST;
		for (final DbTimeZone tz : values()) {
			if (tz.getDbSetting().equalsIgnoreCase(timezone)
					|| tz.name().equalsIgnoreCase(timezone)) {
				retVal = tz;
				break;
			}
		}
		return retVal;
	}

	String dbSetting;

	private DbTimeZone(final String dbSetting) {
		this.dbSetting = dbSetting;
	}

	public String getDbSetting() {
		return dbSetting;
	}
}
