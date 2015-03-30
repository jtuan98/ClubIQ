package com.avatar.mvc.controller;

import java.security.Principal;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/ClubMgr")
public class ClubManagerController {

	@Resource(name="beaconManagerController")
	BeaconManagerController beaconManager;

	@RequestMapping(value = "/RegisterAPNsToken")
	public ModelAndView setAmenityDeptName(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = false, value = "userId") final String userIdNotUsed,
			@RequestParam(required = true, value = "apnsToken") final String apnsToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
			throws Exception {
		return beaconManager.setAmenityDeptName(principal, req, apnsToken, null, clubId);
	}

}
