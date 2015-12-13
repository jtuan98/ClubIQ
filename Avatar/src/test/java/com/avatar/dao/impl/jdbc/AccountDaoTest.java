package com.avatar.dao.impl.jdbc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.util.ReflectionTestUtils;

import com.avatar.dao.ClubDao;
import com.avatar.dao.impl.jdbc.mapper.AccountDtoMapper;
import com.avatar.dao.impl.jdbc.mapper.AccountNotesDtoMapper;
import com.avatar.dao.impl.jdbc.mapper.ActivationTokenMapper;
import com.avatar.dao.impl.jdbc.mapper.ImageMapper;
import com.avatar.dao.impl.jdbc.mapper.RolesMapper;
import com.avatar.dao.impl.jdbc.sql.AccountDaoSql;
import com.avatar.dao.impl.jdbc.sql.BaseDaoSql;
import com.avatar.dto.AccountDtoBuilder;
import com.avatar.dto.ClubDtoBuilder;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.AccountNotes;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

public class AccountDaoTest extends BaseJdbcTest {

	private AccountDaoJdbc dao;
	private ClubDao clubDao;

	private void runFetchTest(final AccountDto account) {
		final Integer userIdPk = account.getId();
		setUpForFetchIntTest(userIdPk, account, false);
		try {
			final AccountDto result = dao.fetch(userIdPk);
			Assert.assertEquals(account.getId(), result.getId());
			Assert.assertEquals(account.getToken(), result.getToken());

			verify(jdbcTemplate, atLeastOnce()).queryForObject(
					eq(BaseDaoSql.GET_SESSION_TIMEZONE), eq(String.class));

			verify(jdbcTemplate, times(1)).queryForObject(
					eq(AccountDaoSql.SEL_USER_BY_PK), any(RolesMapper.class),
					eq(userIdPk));

			verify(jdbcTemplate, times(1)).queryForObject(
					eq(AccountDaoSql.GET_IMAGE_ID), eq(Integer.class),
					eq(userIdPk));

			verify(jdbcTemplate, times(1)).queryForObject(
					eq(AccountDaoSql.GET_HOME_CLUB_ID), eq(Integer.class),
					eq(userIdPk));

			verify(jdbcTemplate, times(1)).query(
					eq(AccountDaoSql.SEL_NOTESHISTORY_BY_USER_ID), any(AccountNotesDtoMapper.class),
					eq(userIdPk));

			if (result instanceof EmployeeAccountDto) {
				verify(jdbcTemplate, times(1)).queryForObject(
						eq(AccountDaoSql.SEL_SUBAMENITY_ID_BY_USERID),
						eq(Integer.class), eq(userIdPk));
			} else {
				verify(jdbcTemplate, never()).queryForObject(
						eq(AccountDaoSql.SEL_SUBAMENITY_ID_BY_USERID),
						eq(Integer.class), eq(userIdPk));
			}
			if (account.getHomeClub() != null) {
				verify(clubDao, times(1)).get(anyInt(), eq(true));
			} else {
				verify(clubDao, never()).get(anyInt(), eq(true));
			}
			if (account.getStatus() == AccountStatus.New
					|| account.getStatus() == AccountStatus.TokenSent) {
				verify(jdbcTemplate, times(1)).queryForObject(
						eq(AccountDaoSql.SEL_TOKEN_BY_USERIDPK),
						any(ActivationTokenMapper.class), eq(userIdPk));
			} else {
				verify(jdbcTemplate, never()).queryForObject(
						eq(AccountDaoSql.SEL_TOKEN_BY_USERIDPK),
						any(ActivationTokenMapper.class), eq(userIdPk));
			}
			verify(jdbcTemplate, times(1)).query(
					eq(AccountDaoSql.SEL_ROLES_BY_USER_ID),
					any(RolesMapper.class), eq(userIdPk));

			verify(jdbcTemplate, times(1))
			.queryForMap(
					eq(AccountDaoSql.SEL_DEVICE_TANGERINE_HANDSET_ID_BY_USER_ID),
					eq(userIdPk));

		} catch (final InvalidParameterException e) {
			Assert.fail("Should not have thrown InvalidParameterException");
		} catch (final NotFoundException e) {
			e.printStackTrace();
			Assert.fail("Should not have thrown NotFoundException");
		}
	}

	@Before
	public void setUp() {
		dao = new AccountDaoJdbc();
		setUp(dao);
		clubDao = mock(ClubDao.class);
		ReflectionTestUtils.setField(dao, "clubDao", clubDao);
	}

	private void setUpForActivateTest(final String userId,
			final String activationToken, final int rowUpdated) {
		given(
				jdbcTemplate.update(eq(AccountDaoSql.UPD_ACCOUNT_ACTIVATION),
						eq(activationToken), eq(userId)))
						.willReturn(rowUpdated);
	}

	private void setUpForAddAmenityToUserTest(final Integer userIdPk,
			final Integer clubAmenityIdPk, final int rowUpdated) {
		given(
				jdbcTemplate.update(eq(AccountDaoSql.UPD_SUBAMENITY_EMPLOYEE),
						eq(clubAmenityIdPk), eq(userIdPk))).willReturn(
								rowUpdated);

	}

	private void setUpForFetchIntTest(final Integer userIdPk,
			final AccountDto retVal,
			final boolean throwEmptyResultDataAccessException) {
		if (throwEmptyResultDataAccessException) {
			given(
					jdbcTemplate.queryForObject(
							eq(AccountDaoSql.SEL_USER_BY_PK),
							any(AccountDtoMapper.class), eq(userIdPk)))
							.willThrow(EmptyResultDataAccessException.class);
		} else {
			given(
					jdbcTemplate.queryForObject(
							eq(AccountDaoSql.SEL_USER_BY_PK),
							any(AccountDtoMapper.class), eq(userIdPk)))
							.willReturn(retVal);
		}
		if (retVal != null) {
			if (retVal.getPicture() != null) {
				given(
						jdbcTemplate.queryForObject(
								eq(AccountDaoSql.GET_IMAGE_ID),
								eq(Integer.class), eq(userIdPk))).willReturn(
										retVal.getPicture().getId());
				given(
						jdbcTemplate.queryForObject(
								eq(BaseDaoSql.SEL_IMAGE_BY_ID),
								any(ImageMapper.class), eq(retVal.getPicture()
										.getId()))).willReturn(
												retVal.getPicture());
			}
			Integer clubId = null;
			if (retVal.getHomeClub() != null) {
				clubId = retVal.getHomeClub().getId();
			}
			if (clubId == null) {
				given(
						jdbcTemplate.queryForObject(
								eq(AccountDaoSql.GET_HOME_CLUB_ID),
								eq(Integer.class), eq(userIdPk))).willThrow(
										NotFoundException.class);
			} else {
				given(
						jdbcTemplate.queryForObject(
								eq(AccountDaoSql.GET_HOME_CLUB_ID),
								eq(Integer.class), eq(userIdPk))).willReturn(
										clubId);
			}
			final List<Privilege> privileges = new LinkedList<>();
			if (retVal.getPriviledges() != null) {
				privileges.addAll(retVal.getPriviledges());
			}
			given(
					jdbcTemplate.query(eq(AccountDaoSql.SEL_ROLES_BY_USER_ID),
							any(RolesMapper.class), eq(userIdPk))).willReturn(
									new LinkedList<Privilege>(privileges));

			if (retVal.getToken() == null) {
				given(
						jdbcTemplate.queryForObject(
								eq(AccountDaoSql.SEL_TOKEN_BY_USERIDPK),
								any(ActivationTokenMapper.class), eq(userIdPk)))
								.willThrow(EmptyResultDataAccessException.class);
			} else {
				doReturn(retVal.getToken()).when(jdbcTemplate).queryForObject(
						eq(AccountDaoSql.SEL_TOKEN_BY_USERIDPK),
						any(ActivationTokenMapper.class), eq(userIdPk));
			}

			final List<AccountNotes> notes = new LinkedList<>();
			if (retVal.getNoteHistory() != null) {
				notes.addAll(retVal.getNoteHistory());
			}
			given(
					jdbcTemplate.query(eq(AccountDaoSql.SEL_NOTESHISTORY_BY_USER_ID),
							any(AccountNotesDtoMapper.class), eq(userIdPk))).willReturn(
									new LinkedList<AccountNotes>(notes));
		}
	}

	@Test(expected=NotFoundException.class)
	public void test001Activate_00_RecNotFound() throws NotFoundException {
		final String userId = "123";
		final String activationToken = "whatever";

		setUpForActivateTest(userId, activationToken, 0);
		dao.activate(userId, activationToken, new Date());
	}

	@Test
	public void test001Activate_01_OneRecFound() {
		final String userId = "123";
		final String activationToken = "whatever";

		setUpForActivateTest(userId, activationToken, 1);
		try {
			dao.activate(userId, activationToken, new Date());
			// Pass!
		} catch (final NotFoundException e) {
			Assert.fail("Should have thrown NotFoundException");
		}
	}

	@Test(expected=InvalidParameterException.class)
	public void test002addAmenityToUser_00_nullParam() throws InvalidParameterException {
		final Integer userIdPk = null;
		final Integer clubAmenityIdPk = null;
		setUpForAddAmenityToUserTest(userIdPk, clubAmenityIdPk, 0);
		dao.addSubAmenityToUser(userIdPk, clubAmenityIdPk);
	}

	@Test
	public void test002addAmenityToUser_01_amenityExists() {
		final Integer userIdPk = 1;
		final Integer clubAmenityIdPk = 2;
		setUpForAddAmenityToUserTest(userIdPk, clubAmenityIdPk, 1);
		try {
			dao.addSubAmenityToUser(userIdPk, clubAmenityIdPk);
			verify(jdbcTemplate, never()).update(
					eq(AccountDaoSql.INS_SUBAMENITY_EMPLOYEE), anyInt(),
					eq(clubAmenityIdPk), eq(userIdPk));
		} catch (final InvalidParameterException e) {
			Assert.fail("Should not have thrown InvalidParameterException");
		}
	}

	@Test
	public void test002addAmenityToUser_02_amenityNotExists() {
		final Integer userIdPk = 1;
		final Integer clubAmenityIdPk = 2;
		setUpForAddAmenityToUserTest(userIdPk, clubAmenityIdPk, 0);
		try {
			dao.addSubAmenityToUser(userIdPk, clubAmenityIdPk);
			verify(jdbcTemplate, times(1)).update(
					eq(AccountDaoSql.INS_SUBAMENITY_EMPLOYEE), anyInt(),
					eq(clubAmenityIdPk), eq(userIdPk));
		} catch (final InvalidParameterException e) {
			Assert.fail("Should not have thrown InvalidParameterException");
		}
	}

	@Test(expected=InvalidParameterException.class)
	public void test003fetch_00_nullIntParam() throws NotFoundException, InvalidParameterException {
		final Integer userIdPk = null;
		setUpForFetchIntTest(userIdPk, null, false);
		dao.fetch(userIdPk);
	}

	@Test(expected=NotFoundException.class)
	public void test003fetch_01_pkNotExists() {
		final Integer userIdPk = -1;
		setUpForFetchIntTest(userIdPk, null, true);
		try {
			dao.fetch(userIdPk);
			Assert.fail("Should have thrown NotFoundException");
		} catch (final InvalidParameterException e) {
			Assert.fail("Should have thrown NotFoundException");
		} catch (final NotFoundException e) {
		}

	}

	@Test(expected=NotFoundException.class)
	public void test003fetch_02_pkExistsNot() throws NotFoundException, InvalidParameterException {
		final Integer userIdPk = -1;
		setUpForFetchIntTest(userIdPk, null, false);
		dao.fetch(userIdPk);
	}

	@Test
	public void test003fetch_03_employeePKExistsNewStatus() {
		final Set<Privilege> privileges = new HashSet<>();
		privileges.add(Privilege.superUser);
		final ActivationToken token = new ActivationToken();
		final AccountDtoBuilder builder = new AccountDtoBuilder(true);
		final AccountDto account = builder.withId(1)
				.withStatus(AccountStatus.New).withToken(token)
				.withPrivilege(privileges).getBuiltInstance();
		runFetchTest(account);
	}

	@Test
	public void test003fetch_04_memberPKExistsNewStatus() {
		final Set<Privilege> privileges = new HashSet<>();
		privileges.add(Privilege.user);
		final ClubDto club = new ClubDtoBuilder().withId(1).getBuiltInstance();
		final ActivationToken token = new ActivationToken();
		final AccountDtoBuilder builder = new AccountDtoBuilder(false);
		final AccountDto account = builder.withId(1)
				.withStatus(AccountStatus.New).withToken(token)
				.withHomeClub(club).withPrivilege(privileges)
				.getBuiltInstance();
		runFetchTest(account);
	}

	@Test
	public void test003fetch_05_memberPKExistsActivatedStatus() {
		final Set<Privilege> privileges = new HashSet<>();
		privileges.add(Privilege.user);
		final ClubDto club = new ClubDtoBuilder().withId(1).getBuiltInstance();
		final ActivationToken token = new ActivationToken();
		final AccountDtoBuilder builder = new AccountDtoBuilder(false);
		final AccountDto account = builder.withId(1)
				.withStatus(AccountStatus.Activated).withToken(token)
				.withHomeClub(club).withPrivilege(privileges)
				.getBuiltInstance();
		runFetchTest(account);
	}
}
