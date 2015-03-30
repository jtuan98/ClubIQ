package com.avatar.util;

import java.io.Serializable;

public class CronExpressionCreator implements Serializable {

	public static void main(final String[] args) {
		final CronExpressionCreator pCron = new CronExpressionCreator();
		pCron.setTime("*");//"1:00 PM");
		pCron.setMON(true);
		pCron.setStartDate("12-05-2011");
		pCron.setRecurring(true);
		System.out.println(pCron.getCronExpression());
	}

	private static final long serialVersionUID = -1676663054009319677L;

	String startDate;

	String time;
	boolean recurring;
	boolean SUN;
	boolean MON;
	boolean TUE;
	boolean WED;
	boolean THU;
	boolean FRI;
	boolean SAT;
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof CronExpressionCreator) {
			if (((CronExpressionCreator) obj).getCronExpression()
					.equalsIgnoreCase(getCronExpression())) {
				return true;
			}
		} else {
			return false;
		}
		return false;

	}

	public String getCronExpression() {
		final String time = getTime();
		final String[] time1 = time.split("\\:");
		final String[] time2 = time1[1].split("\\ ");

		String hour = "";
		if (time2[1].equalsIgnoreCase("PM")) {
			Integer hourInt = Integer.parseInt(time1[0]) + 12;
			if (hourInt == 24) {
				hourInt = 0;
			}
			hour = hourInt.toString();
		} else {
			hour = time1[0];
		}

		final String minutes = time2[0];
		String cronExp = "";
		if (isRecurring()) {
			String daysString = "";
			final StringBuilder sb = new StringBuilder(800);
			boolean moreConditions = false;

			if (isSUN()) {
				sb.append("SUN");
				moreConditions = true;
			}

			if (isMON()) {
				if (moreConditions) {
					sb.append(",");
				}
				sb.append("MON");
				moreConditions = true;
			}

			if (isTUE()) {
				if (moreConditions) {
					sb.append(",");
				}

				sb.append("TUE");
				moreConditions = true;
			}

			if (isWED()) {
				if (moreConditions) {
					sb.append(",");
				}

				sb.append("WED");
				moreConditions = true;
			}

			if (isTHU()) {
				if (moreConditions) {
					sb.append(",");
				}
				sb.append("THU");
				moreConditions = true;
			}

			if (isFRI()) {
				if (moreConditions) {
					sb.append(",");
				}
				sb.append("FRI");
				moreConditions = true;
			}

			if (isSAT()) {
				if (moreConditions) {
					sb.append(",");
				}
				sb.append("SAT");
				moreConditions = true;
			}

			daysString = sb.toString();

			cronExp = "0 " + minutes + " " + hour + " ? * " + daysString;
		} else {
			final String startDate = getStartDate();
			final String[] dateArray = startDate.split("\\-");
			String day = dateArray[0];
			if (day.charAt(0) == '0') {
				day = day.substring(1);
			}

			String month = dateArray[1];

			if (month.charAt(0) == '0') {
				month = month.substring(1);
			}

			final String year = dateArray[2];
			cronExp = "0 " + minutes + " " + hour + " " + day + " " + month
					+ " ? " + year;

		}
		return cronExp;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	@Override
	public int hashCode() {
		return getCronExpression().hashCode();
	}

	public boolean isFRI() {
		return FRI;
	}

	public boolean isMON() {
		return MON;
	}

	public boolean isRecurring() {
		return recurring;
	}

	public boolean isSAT() {
		return SAT;
	}

	public boolean isSUN() {
		return SUN;
	}

	public boolean isTHU() {
		return THU;
	}

	public boolean isTUE() {
		return TUE;
	}

	public boolean isWED() {
		return WED;
	}

	public void setFRI(final boolean fRI) {
		FRI = fRI;
	}

	/**
	 * @param mON
	 *            the mON to set
	 */
	public void setMON(final boolean mON) {
		MON = mON;
	}

	public void setRecurring(final boolean recurring) {
		this.recurring = recurring;
	}

	public void setSAT(final boolean sAT) {
		SAT = sAT;
	}

	/**
	 * The date set should be of the format (MM-DD-YYYY for example 25-04-2011)
	 *
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	public void setSUN(final boolean sUN) {
		SUN = sUN;
	}

	public void setTHU(final boolean tHU) {
		THU = tHU;
	}

	/**
	 * The time set should be of the format (HH:MM AM/PM for example 12:15 PM)
	 *
	 * @param time
	 *            the time to set
	 */
	public void setTime(final String time) {
		this.time = time;
	}

	public void setTUE(final boolean tUE) {
		TUE = tUE;
	}

	public void setWED(final boolean wED) {
		WED = wED;
	}

}
