package com.avatar.dto.enums;

public enum Location {
	BWL("Back Wall Left"), BWR("Back Wall Right"), BWM("Back Wall Middle"), FWL(
			"Back Wall Left"), FWR("Back Wall Right"), FWM("Back Wall Middle"), LWL(
			"Back Wall Left"), LWR("Back Wall Right"), LWM("Back Wall Middle"), RWL(
			"Back Wall Left"), RWR("Back Wall Right"), RWM("Back Wall Middle");
	String description;

	private Location(final String description) {
		this.description = description;
	}

}
