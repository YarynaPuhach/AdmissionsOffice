package ua.lviv.lgs.admissionsOffice.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ua.lviv.lgs.admissionsOffice.domain.AccessLevel;
import ua.lviv.lgs.admissionsOffice.domain.Applicant;
import ua.lviv.lgs.admissionsOffice.domain.Speciality;
import ua.lviv.lgs.admissionsOffice.domain.User;
import ua.lviv.lgs.admissionsOffice.service.ApplicationService;
import ua.lviv.lgs.admissionsOffice.service.RatingListService;
import ua.lviv.lgs.admissionsOffice.service.UserService;

@Controller
@RequestMapping("/main")
public class MainController {
	@Autowired
	private UserService userService;
	@Autowired
	private ApplicationService applicationService;
	@Autowired
	private RatingListService ratingListService;
		
	@GetMapping
	public String viewMainPage(HttpSession session, Model model) throws UnsupportedEncodingException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) auth.getPrincipal();
		User userFromDb = userService.findById(user.getId());
		
		session.setAttribute("user", userFromDb);
		
		if (userFromDb.getAccessLevels().contains(AccessLevel.valueOf("USER"))) {
			List<Speciality> specialitiesByApplicant = ratingListService.findSpecialitiesAppliedByApplicant(userFromDb.getId());
			Map<Speciality, Set<Applicant>> enrolledApplicants = new HashMap<>();

			for (Speciality speciality : specialitiesByApplicant) {
				enrolledApplicants.put(speciality, ratingListService.getEnrolledApplicantsBySpeciality(speciality));	
			}			

			session.setAttribute("photo", userService.parseFileData(userFromDb));
			session.setAttribute("specialities", specialitiesByApplicant);
			model.addAttribute("submittedApps", ratingListService.parseNumberOfApplicationsBySpeciality());
			model.addAttribute("isRejectedAppsPresent", applicationService.checkForRejectedApplications(applicationService.findByApplicant(userFromDb.getApplicant())));
			model.addAttribute("enrolledApplicants", enrolledApplicants);
		}
		
		if (userFromDb.getAccessLevels().contains(AccessLevel.valueOf("ADMIN"))) {
			session.setAttribute("notAcceptedApps", ratingListService.findNotAcceptedApps());
		}
		
		return "main";
	}
}
