package com.avatar.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.avatar.dto.club.BlackoutDate;
import com.avatar.dto.club.BlackoutTime;
import com.avatar.dto.club.CheckInfo;
import com.avatar.exception.NotFoundException;

public interface ReservationDao {

	List<BlackoutDate> fetchBlackoutDates(int clubIdPk, int subAmenityIdPk, String year, String month);

	Map<String, List<BlackoutTime>> fetchBlackoutTimes(int clubIdPk, int subAmenityIdPk,
			Date requestedDateFrom, Date requestedDateTo);

	List<BlackoutTime> fetchBlackoutTimes(int clubIdPk, int subAmenityIdPk,
			String year, String month, String day);

	CheckInfo getReservation(String availId) throws NotFoundException;

	Number reserve (int clubIdPk, int subAmenityIdPk, int userIdPk, int numberOfPeople, Date reservationDate, String reservationNumber, Date requestedToDateTime) throws NotFoundException;

	void setBlackoutTimes(int clubIdPk, int subAmenityIdPk,
			Date requestedDate, String blackoutTimes);
}
