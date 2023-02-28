package ua.lviv.lgs.admissionsOffice.controller;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ua.lviv.lgs.admissionsOffice.domain.User;
import ua.lviv.lgs.admissionsOffice.dto.CaptchaResponse;
import ua.lviv.lgs.admissionsOffice.service.UserService;

@Controller
public class RegistrationController {
	private final static String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";
	
	@Autowired
	private UserService userService;
	@Autowired
    private RestTemplate restTemplate;
	
	@Value("${recaptcha.secret}")
    private String secret;
	
	@GetMapping("/registration")
	public String viewRegistrationForm() {
		return "registration";
	}

	@PostMapping("/registration")
	public String registerUser(
			@RequestParam("g-recaptcha-response") String reCaptchaResponse,
			@RequestParam String confirmPassword,
			@Valid User user,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redir) {
		String url = String.format(CAPTCHA_URL, secret, reCaptchaResponse);
		CaptchaResponse captchaResponse = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponse.class);

		if (StringUtils.isEmpty(confirmPassword) || bindingResult.hasErrors() || !captchaResponse.isSuccess()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            model.addAttribute("confirmPasswordError", "The user password must be at least 6 characters!");
            model.addAttribute("captchaError", "Please fill in the captcha!");
            return "registration";
        }
        
        if (user.getPassword() != null && !user.getPassword().equals(confirmPassword)) {
        	model.addAttribute("confirmPasswordError", "The entered passwords do not match!");
        	return "registration";
        }
        
		if (!userService.addUser(user)) {
			model.addAttribute("messageType", "danger");
			model.addAttribute("message", "This user already exists!");
			return "registration";
		}
		
		redir.addFlashAttribute("message", "To activate the user, follow the link in the letter sent to the email address you specified!");
		return "redirect:/login/";
	}
	
	@GetMapping("/activate/{code}")
    public String activate(@PathVariable String code, Model model) {
        boolean isActivated = userService.activateUser(code);

        if (isActivated) {
        	model.addAttribute("messageType", "success");
            model.addAttribute("message", "\n" +
					"User activated successfully!");
        } else {
        	model.addAttribute("messageType", "danger");
            model.addAttribute("message", "Activation code not found!");
        }

        return "login";
    }
}