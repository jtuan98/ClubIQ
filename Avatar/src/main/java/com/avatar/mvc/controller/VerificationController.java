package com.avatar.mvc.controller;

import java.security.Principal;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.dto.enums.DbTimeZone;
import com.avatar.exception.InvalidDeviceId;

@Controller
@RequestMapping(value = "/TestMgr")
public class VerificationController extends BaseController {

	@RequestMapping(value = "/testApns")
	public ModelAndView testApns(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "alert", defaultValue = "true") final boolean alert,
			@RequestParam(required = true, value = "deviceId") final String deviceId,
			@RequestParam(required = false, value = "staff", defaultValue = "true") final boolean staff,
			@RequestParam(required = true, value = "msg") final String msg)
			throws Exception {
		init();
		String msgRetVal = "";
		try {
			if (!alert) {
				mobileNotificationService
						.sendNotification(deviceId, msg, staff);
			} else {
				mobileNotificationService.testAlert(deviceId, staff);
			}
		} catch (final InvalidDeviceId e) {
			msgRetVal = e.getMessage();
		}
		return new ModelAndView(jsonView, toModel(msgRetVal));
	}

	@RequestMapping(value = "/testEmail")
	public ModelAndView testEmail(final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "email") final String email,
			@RequestParam(required = true, value = "msg") final String msg)
			throws Exception {
		init();
		String msgRetVal = "";
		try {
			emailNotificationService.sendNotification(email, msg, false);
		} catch (final InvalidDeviceId e) {
			msgRetVal = e.getMessage();
		}
		return new ModelAndView(jsonView, toModel(msgRetVal));
	}

	@RequestMapping(value = "/testNow")
	public ModelAndView testNow(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "timezone") final DbTimeZone timezone,
			@RequestParam(required = false, value = "help") final boolean help)
			throws Exception {
		init();
		String msgRetVal = "";
		if (help) {
			msgRetVal = Arrays.toString(DbTimeZone.values());
		} else {
			try {
				msgRetVal = yyyyMMdd_hh24missDtf.print(nowService.getNow(
						timezone).getTime());
			} catch (final Exception e) {
				msgRetVal = e.getMessage();
			}
		}
		return new ModelAndView(jsonView, toModel(msgRetVal));
	}

}
