package com.avatar.mvc.controller;

import java.security.Principal;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.dto.AuthenticationTokenPrincipal;
import com.avatar.dto.WsResponse;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Controller
@RequestMapping(value = { "/LoginMgr" })
public class TokenizerController extends BaseController {

	@RequestMapping(value = { "/getAuthToken", "/GetAuthToken" })
	public ModelAndView getAuthToken(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String mobileNumber)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			final AuthenticationTokenPrincipal principalToken = authenticationService
					.getToken(mobileNumber, null);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					principalToken.getToken().toString(), "authToken");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/GetUserRoles" })
	public ModelAndView getUserRoles(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken)
					throws Exception {
		init();
		WsResponse<Set<Privilege>> apiResponse = null;
		try {
			final Set<Privilege> roles = authenticationService
					.getRoles(authToken);
			apiResponse = new WsResponse<Set<Privilege>>(ResponseStatus.success, "",
					roles, "roles");
		} catch (final Exception e) {
			apiResponse = new WsResponse<Set<Privilege>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/Login", "/login" })
	public ModelAndView login(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "userId") final String userId,
			@RequestParam(required = true, value = "password") final String password)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			final AuthenticationTokenPrincipal principalToken = authenticationService
					.getToken(userId, password);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					principalToken.getToken().toString(), "authToken");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}


	@RequestMapping(value = { "/validate" })
	public ModelAndView testAuthToken(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "roles") final Privilege[] roles)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, roles);
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.success,
					"", "User got the necessary roles");
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		return new ModelAndView(jsonView, toModel(apiDeniedResponse));
	}

}
