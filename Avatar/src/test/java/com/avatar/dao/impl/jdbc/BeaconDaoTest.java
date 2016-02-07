package com.avatar.dao.impl.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.TruncateDao;
import com.avatar.dto.AccountDtoBuilder;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class BeaconDaoTest {
	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	static class MySqlContextConfiguration extends
	MySqlLocalJdbcContextConfiguration {
	}

	private static final String BEACON_ID_UNIT_TEST = "BeaconUT";

	private static final String SUB_AMENITY = "unittest subamenity";

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDaoJdbc;

	@Resource(name = "beaconDaoJdbc")
	private BeaconDao beaconDaoJdbc;

	@Resource(name = "accountDaoJdbc")
	private TruncateDao accountTruncateDaoJdbc;

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDaoJdbc;

	@Before
	public void cleanup() throws NotFoundException, InvalidParameterException,
	PermissionDeniedException {
		try {
			final int beaconIdPk = beaconDaoJdbc
					.getBeaconIdPk(BEACON_ID_UNIT_TEST);
			beaconDaoJdbc.delete(beaconDaoJdbc.getBeacon(beaconIdPk));
		} catch (final NotFoundException e) {

		}
		accountTruncateDaoJdbc.truncate();
		beaconDaoJdbc.setSubAmenityId("unittest club", "unittest apns toke",
				"Bar", SUB_AMENITY);
	}

	private AccountDto createAccount() {
		return createAccount("theUserId");
	}

	private AccountDto createAccount(final String userId) {
		try {
			final AccountDto account = new AccountDtoBuilder(true)
			.withUserId(userId).withName("Unit Test")
			.withDefaultToken(false).getBuiltInstance();
			accountDaoJdbc.newAccount(account, account.getToken());
			accountDaoJdbc.activate(account.getUserId(), account.getToken()
					.getToken(), new Date());
			final AccountDto accountFroDb = accountDaoJdbc.fetch(account
					.getUserId());
			return accountFroDb;
		} catch (InvalidParameterException | NotFoundException e) {
			Assert.fail("Unable to create account");
		}
		return null;
	}

	private BeaconDto createBeacon() throws NotFoundException {
		final AccountDto staff = createAccount("installerStaff");
		final BeaconDto beacon = new BeaconDto();
		beacon.setBeaconActionId(BEACON_ID_UNIT_TEST);
		final ClubDto club = new ClubDto();
		club.setId(1);
		final int subAmenityIdPk = clubDaoJdbc.getClubSubAmenityIdPk(1,
				SUB_AMENITY);
		final SubAmenityDto subAmenity = clubDaoJdbc
				.getSubAmenity(subAmenityIdPk);
		beacon.setClub(club);
		beacon.setSubAmenity(subAmenity);
		beacon.setLocation("location");
		beacon.setDescription("Unit Testing");
		beacon.setInstallerStaff(staff);
		beacon.setInstallationDate(new Date());
		beaconDaoJdbc.updateBeaconInfo(beacon);
		return beacon;
	}

	@Test(expected = InvalidParameterException.class)
	public void testAddUserIdToBeaconMapping01aNullBeaconId()
			throws NotFoundException, InvalidParameterException {
		beaconDaoJdbc.addUserIdToBeaconMapping(null, "userId");
	}

	@Test(expected = InvalidParameterException.class)
	public void testAddUserIdToBeaconMapping01bNullUserId()
			throws NotFoundException, InvalidParameterException {
		beaconDaoJdbc.addUserIdToBeaconMapping("beaconId", null);
	}

	@Test(expected = NotFoundException.class)
	public void testAddUserIdToBeaconMapping02NonExistentUserId()
			throws NotFoundException, InvalidParameterException {
		beaconDaoJdbc.addUserIdToBeaconMapping("beaconId", "userId");
	}

	@Test(expected = NotFoundException.class)
	public void testAddUserIdToBeaconMapping03NonExistentBeaconId()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = createAccount();
		beaconDaoJdbc.addUserIdToBeaconMapping("beaconId", account.getUserId());
	}

	@Test
	public void testAddUserIdToBeaconMapping04ExistentBeaconId()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = createAccount();
		final BeaconDto beacon = createBeacon();
		beaconDaoJdbc.addUserIdToBeaconMapping(beacon.getBeaconActionId(),
				account.getUserId());
		final List<AccountDto> users = beaconDaoJdbc
				.getUsers(SUB_AMENITY, null);
		assertNotNull(users);
		assertEquals(1, users.size());
		assertEquals(account.getUserId(), users.get(0).getUserId());
	}

	@Test(expected = InvalidParameterException.class)
	public void testDelete01aNullBeacon() throws NotFoundException,
	InvalidParameterException, PermissionDeniedException {
		beaconDaoJdbc.delete(null);
	}

	@Test(expected = InvalidParameterException.class)
	public void testDelete01bNullBeaconId() throws NotFoundException,
	InvalidParameterException, PermissionDeniedException {
		beaconDaoJdbc.delete(new BeaconDto());
	}

	@Test
	public void testDelete02BeaconIdWithLinkedAccount()
			throws NotFoundException, InvalidParameterException,
			PermissionDeniedException {
		final BeaconDto beacon = createBeacon();
		final AccountDto account = createAccount();
		beaconDaoJdbc.addUserIdToBeaconMapping(beacon.getBeaconActionId(),
				account.getUserId());
		beaconDaoJdbc.delete(beacon);
	}

	@Test(expected = InvalidParameterException.class)
	public void testDeleteBeaconInfoByUserId01aNullBeaconIdPk()
			throws NotFoundException, InvalidParameterException,
			PermissionDeniedException {
		beaconDaoJdbc.deleteBeaconInfoByUserId(null, new Date(), new Date());
	}

	@Test(expected = InvalidParameterException.class)
	public void testDeleteBeaconInfoByUserId01bNullFromDate()
			throws NotFoundException, InvalidParameterException,
			PermissionDeniedException {
		beaconDaoJdbc.deleteBeaconInfoByUserId(1, null, new Date());
	}

	@Test(expected = InvalidParameterException.class)
	public void testDeleteBeaconInfoByUserId01cNullToDate()
			throws NotFoundException, InvalidParameterException,
			PermissionDeniedException {
		beaconDaoJdbc.deleteBeaconInfoByUserId(1, new Date(), null);
	}

	@Test
	public void testDeleteBeaconInfoByUserId02NonExistentUserId()
			throws NotFoundException, InvalidParameterException {
		beaconDaoJdbc.deleteBeaconInfoByUserId(-1, new Date(), new Date());
	}

	@Test
	public void testDeleteBeaconInfoByUserId03ValidParams()
			throws NotFoundException, InvalidParameterException,
			PermissionDeniedException {
		final Date fromDate = new Date();
		final BeaconDto beacon = createBeacon();
		final AccountDto account = createAccount();
		beaconDaoJdbc.addUserIdToBeaconMapping(beacon.getBeaconActionId(),
				account.getUserId());
		beaconDaoJdbc.deleteBeaconInfoByUserId(account.getId(), fromDate,
				new Date());
		final List<AccountDto> users = beaconDaoJdbc
				.getUsers(SUB_AMENITY, null);
		assertNotNull(users);
		assertEquals(0, users.size());
	}


	@Test(expected = InvalidParameterException.class)
	public void testGetAmenityIdPk01aNullAmenityId()
			throws NotFoundException, InvalidParameterException,
			PermissionDeniedException {
		beaconDaoJdbc.getAmenityIdPk(null);
	}

	@Test(expected=NotFoundException.class)
	public void testGetAmenityIdPk02NonExistentBeaconId()
			throws NotFoundException, InvalidParameterException,
			PermissionDeniedException {
		beaconDaoJdbc.getAmenityIdPk(-1);
	}

	@Test(expected=NotFoundException.class)
	public void testGetAmenityIdPk03ExistentBeaconId()
			throws NotFoundException, InvalidParameterException,
			PermissionDeniedException {
		beaconDaoJdbc.getAmenityIdPk(-1);
	}

}
