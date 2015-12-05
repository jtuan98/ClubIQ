package com.avatar.dao;

import java.util.Date;
import java.util.List;

import com.avatar.dto.club.BlackoutDate;
import com.avatar.dto.club.BlackoutTime;
import com.avatar.dto.club.CheckInfo;
import com.avatar.exception.NotFoundException;

public interface ReservationDao {

	List<BlackoutDate> fetchBlackoutDates(int clubIdPk, int amenityIdPk, String month);

	List<BlackoutTime> fetchBlackoutTimes(int clubIdPk, int amenityIdPk,
			String month, String day);

	CheckInfo getReservation(int userIdPk, String availId) throws NotFoundException;

	Number reserve (int clubIdPk, int amenityIdPk, int userIdPk, int numberOfPeople, Date reservationDate, String reservationNumber) throws NotFoundException;
}
