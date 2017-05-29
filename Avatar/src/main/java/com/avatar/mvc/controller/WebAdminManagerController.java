package com.avatar.mvc.controller;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.NotificationBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.dto.serializer.AccountDtoMemberDetailsSerializer;
import com.avatar.dto.serializer.AccountDtoMemberNoteHistoryOnlySerializer;
import com.avatar.dto.serializer.AccountDtoMemberSummarySerializer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;
import com.avatar.mvc.view.JsonView;

@Controller
@RequestMapping(value = "/WebAdminMgr")
public class WebAdminManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.clubAdmin,
		Privilege.staff, Privilege.superUser };

	private JsonView jsonAccountDetailsView;
	private JsonView jsonAccountNoteHistoryView;

	@Resource(name = "emailSendService")
	private NotificationBusiness emailSendService;

	@RequestMapping(value = { "/GetMemberDetails", "/getMemberDetails" })
	public ModelAndView getMemberDetails(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "memberId") final String memberId,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Check authToken with clubId
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		final WsResponse<AccountDto> apiResponse = getMemberDetailsInfo(authToken,
				memberId, clubId);
		return new ModelAndView(jsonAccountDetailsView, toModel(apiResponse));
	}

	private WsResponse<AccountDto> getMemberDetailsInfo(final String authToken,
			final String memberId, final String clubId)
					throws InvalidParameterException {
		WsResponse<AccountDto> apiResponse = null;
		try {
			final AccountDto member = accountService.get(memberId);
			apiResponse = new WsResponse<AccountDto>(ResponseStatus.success,
					"", member);
		} catch (final Exception e) {
			apiResponse = new WsResponse<AccountDto>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return apiResponse;
	}

	@RequestMapping(value = { "/GetMemberNoteHistory", "/getMemberNoteHistory" })
	public ModelAndView getMemberNoteHistory(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "memberId") final String memberId,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Check authToken with clubId
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		final WsResponse<AccountDto> apiResponse = getMemberDetailsInfo(authToken,
				memberId, clubId);
		return new ModelAndView(jsonAccountNoteHistoryView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/GetMembers", "/getMembers" })
	public ModelAndView getMembers(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Check authToken with clubId
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<List<AccountDto>> apiResponse = null;
		try {
			final List<AccountDto> members = accountService.getMembers(clubId);
			apiResponse = new WsResponse<List<AccountDto>>(
					ResponseStatus.success, "", members, "Users");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<AccountDto>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonAccountDetailsView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/GetMembersByDate", "/getMembersByDate" })
	public ModelAndView getMembersByDate(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "date") final String fromDateyyyymmddhh24miss,
			@RequestParam(required = false, value = "clubId") final String clubId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<List<AccountDto>> apiResponse = null;
		try {
			final List<AccountDto> members = accountService.getMembers(clubId, yyyyMMdd_hh24missDtf
					.parseDateTime(fromDateyyyymmddhh24miss), true);
			apiResponse = new WsResponse<List<AccountDto>>(
					ResponseStatus.success, "", members, "Users");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<AccountDto>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonAccountDetailsView, toModel(apiResponse));
	}

	@Override
	protected void init() {
		super.init();
		jsonAccountDetailsView = super.init(jsonAccountDetailsView);
		jsonView.register(MemberAccountDto.class, new AccountDtoMemberSummarySerializer());
		jsonAccountDetailsView.register(MemberAccountDto.class, new AccountDtoMemberDetailsSerializer());
		jsonAccountNoteHistoryView = super.init(jsonAccountNoteHistoryView);
		jsonAccountNoteHistoryView.register(MemberAccountDto.class, new AccountDtoMemberNoteHistoryOnlySerializer());
		jsonAccountNoteHistoryView.register(EmployeeAccountDto.class, new AccountDtoMemberNoteHistoryOnlySerializer());
	}

	@RequestMapping(value = { "/render/MemberPhoto", "/render/memberPhoto" })
	public ModelAndView renderMemberPhoto(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "memberId") final String memberId,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();
		byte[] image = null;
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Check authToken with clubId
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		try {
			final AccountDto member = accountService.get(memberId);
			image = member.getPicture() != null? member.getPicture().getPicture(): null;
		} catch (final Exception e) {
		}
		return new ModelAndView(imageRenderer, toModel(image));
	}

	@RequestMapping(value = { "/SetMemberNotes", "/setMemberNotes" })
	public ModelAndView setMemberNotes(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "memberId") final String memberId,
			@RequestParam(required = true, value = "noteText") final String noteText,
			@RequestParam(required = true, value = "noteDate") final String noteDateyyyymmddhh24miss)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Check authToken with clubId
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<String> apiResponse = null;
		try {
			accountService.addNote(memberId, noteText, yyyyMMdd_hh24missDtf
					.parseDateTime(noteDateyyyymmddhh24miss));
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/SuspendMember", "/suspendMember" })
	public ModelAndView suspendMember(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "memberId") final String memberId,
			@RequestParam(required = true, value = "suspendDate") final String suspendDateyyyymmdd)
					throws Exception {
		init();
		DateTime suspendDate;
		if (StringUtils.isEmpty(suspendDateyyyymmdd)) {
			suspendDate = dateService.getNowDateTime();
		} else {
			suspendDate = yyyyMMddDtf.parseDateTime(suspendDateyyyymmdd);
		}
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Check authToken with clubId
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<String> apiResponse = null;
		try {
			accountService.suspend(memberId, suspendDate);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/UnsuspendMember", "/unsuspendMember" })
	public ModelAndView unsuspendMember(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "memberId") final String memberId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Check authToken with clubId
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<String> apiResponse = null;
		try {
			accountService.unsuspend(memberId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/testSend" })
	public ModelAndView xzyTestSendMember(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "memberId") final String memberId)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			final AccountDto member = accountService.get(memberId);
			emailSendService.sendNotification(member);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}



}
