package com.avatar.mvc.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.avatar.business.AuthenticationTokenizerBusiness;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.service.BeaconService;

public class CalendarMgrTest {
	protected static final Privilege[] superUserOrStaff = new Privilege[] { Privilege.superUser, Privilege.avatarStaff, Privilege.clubAdmin, Privilege.staff };
	private static final String WHATEVER_TOKEN = "whatever";
	@Mock
	private BeaconService beaconService;
	@Mock
	private AuthenticationTokenizerBusiness authenticationService;

	@InjectMocks
	private CalendarManagerController calendarMgrController;

	private MockMvc mockMvc;

	@Before
	public void setup() throws NotFoundException, AuthenticationTokenExpiredException, InvalidParameterException {

		// Process mock annotations
		MockitoAnnotations.initMocks(this);

		// Setup Spring test in standalone mode
		this.mockMvc = MockMvcBuilders.standaloneSetup(calendarMgrController).build();
		final Set<Privilege> roles = new HashSet<Privilege>();
		CollectionUtils.addAll(roles, superUserOrStaff);
		when(authenticationService.getRoles(WHATEVER_TOKEN)).thenReturn(roles);
		final AccountDto account = new EmployeeAccountDto(1);
		account.add(Privilege.superUser);
		when(authenticationService.getAccount(WHATEVER_TOKEN)).thenReturn(account);
	}

	@Test
	public void testSetBlackOutDateRange() throws Exception {

		this.mockMvc.perform(get("/CalendarMgr/setBlackOutDateRange")
				.param("authToken", "whatever")
				.param("clubId", "myclub")
				.param("subAmenityId", "mysubAmenityId")
				.param("blackOutFromDate", "01012016")
				.param("blackOutToDate", "30012016")
				.param("blackOutTime", "NNNNNNNNNNYYYYYYYYYYYYYYYYYYYNNNNNNNNNNNNNNNNNNY"))
				.andExpect(status().isOk());
		verify(beaconService, times(30)).setBlackoutTimes(Matchers.anyString(), Matchers.anyString(), Matchers.any(Date.class), Matchers.anyString());
	}
}
