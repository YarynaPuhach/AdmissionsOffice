package ua.lviv.lgs.admissionsOffice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ua.lviv.lgs.admissionsOffice.domain.AccessLevel;
import ua.lviv.lgs.admissionsOffice.domain.User;
import ua.lviv.lgs.admissionsOffice.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping
	public String userList(Model model) {
		model.addAttribute("users", userService.findAll());

		return "userList";
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping("{user}")
	public String userEditForm(@PathVariable User user, Model model) {
		model.addAttribute("user", user);
		model.addAttribute("accessLevels", AccessLevel.values());

		return "userEditor";
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@PostMapping
	public String userSave(@RequestParam Map<String, String> form, @RequestParam("userId") User user, Model model) {
		Map<String, String> errors = new HashMap<>();
		if (StringUtils.isEmpty(form.get("firstName"))) {
			errors.put("firstNameError", "Username cannot be empty!");
		}
		
		if (StringUtils.isEmpty(form.get("lastName"))) {
			errors.put("lastNameError", "The user's last name cannot be empty!\n");
		}
		
		if (!errors.isEmpty()) {
			model.mergeAttributes(errors);
			model.addAttribute("user", user);
			model.addAttribute("accessLevels", AccessLevel.values());

			return "userEditor";						
		}
		
		userService.saveUser(user, form);

		return "redirect:/user";
	}
	
	@GetMapping("profile")
	public String getProfile(@AuthenticationPrincipal User user, Model model) {
		model.addAttribute("user", userService.findById(user.getId()));
		
		return "profile";
	}

	@PostMapping("profile")	
	public String updateProfile(@AuthenticationPrincipal User user, @RequestParam String firstName,
			@RequestParam String lastName, @RequestParam String email, @RequestParam String password,
			@RequestParam String confirmPassword, Model model) {
		Map<String, String> errors = new HashMap<>();
		if (StringUtils.isEmpty(firstName)) {
			errors.put("firstNameError", "Username cannot be empty!");
		}
		
		if (StringUtils.isEmpty(lastName)) {
			errors.put("lastNameError", "The user's last name cannot be empty!");
		}
		
		if (StringUtils.isEmpty(email)) {
			errors.put("emailError", "User email cannot be empty!");
		}
		
		if (password.length() < 6) {
			errors.put("passwordError", "\n" +
					"The user password must be at least 6 characters long!");
		}
		
		if (confirmPassword.length() < 6) {
			errors.put("confirmPasswordError", "\n" +
					"The user password must be at least 6 characters long!");
		}
			
		if (password != "" && confirmPassword != "" && !password.equals(confirmPassword)) {
			errors.put("confirmPasswordError", "\n" +
					"The entered passwords do not match!");
        }
		
		if (!errors.isEmpty()) {
			model.mergeAttributes(errors);
			model.addAttribute("firstName", firstName);
			model.addAttribute("lastName", lastName);
			model.addAttribute("email", email);
			return "profile";			
		}
		
		userService.updateProfile(user, firstName, lastName, email, password);
		
		return "redirect:/main";
	}
}
