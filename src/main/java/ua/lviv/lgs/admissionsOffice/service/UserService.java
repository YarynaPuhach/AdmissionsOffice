package ua.lviv.lgs.admissionsOffice.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import ua.lviv.lgs.admissionsOffice.dao.ApplicantRepository;
import ua.lviv.lgs.admissionsOffice.dao.UserRepository;
import ua.lviv.lgs.admissionsOffice.domain.AccessLevel;
import ua.lviv.lgs.admissionsOffice.domain.Applicant;
import ua.lviv.lgs.admissionsOffice.domain.User;

@Service
public class UserService implements UserDetailsService {
	Logger logger = LoggerFactory.getLogger(UserService.class);
	
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicantRepository applicantRepository;
	@Autowired
	private MailSender mailSender;
	@Autowired
    private PasswordEncoder passwordEncoder;
	
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    	logger.trace("Getting user by email \"" + email + "\" from database...");
    	
        return userRepository.findByEmail(email);
    }
    
    public User findById(Integer id) {
    	logger.trace("Getting user by id=" + id + " from database...");
    	
    	return userRepository.findById(id).get();
    }
    
    public List<User> findAll() {
    	logger.trace("Getting all users from database...");
    	
    	return userRepository.findAll();
    }

    public boolean addUser(User user) {
    	logger.trace("Adding new user to database...");
    	
        User userFromDb = userRepository.findByEmail(user.getEmail());

        if (userFromDb != null) {
        	logger.warn("User with email \"" + userFromDb.getEmail() + "\" already exists in database...");
            return false;
        }

        logger.trace("Encoding recieved user's password...");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false);
        user.setAccessLevels(Collections.singleton(AccessLevel.USER));
        logger.trace("Generating activation code...");
        user.setActivationCode(UUID.randomUUID().toString());

        logger.trace("Saving new user in database...");
        userRepository.save(user);
        sendActivationCode(user);
        return true;
    }

	public void sendActivationCode(User user) {
		logger.trace("Sending activation code to user's email...");
		
		if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
					"Hello, %s %s! \n\n" +
							"Welcome to the application \"Admissions Office\".\n" +
							"To continue registration and activate your account, please follow the link:\n" +
							"http://localhost:8080/activate/%s",
					user.getFirstName(),
                    user.getLastName(),
                    user.getActivationCode()
            );

            mailSender.send(user.getEmail(), "Account activation code", message);
        }
	}

    public boolean activateUser(String code) {
    	logger.trace("Activating user's account...");
    	
    	logger.trace("Getting user by activation code \"" + code + "\" from database...");
        User user = userRepository.findByActivationCode(code);

        if (user == null) {
        	logger.warn("There is no any user with activation code \"" + code + "\" in database...");
            return false;
        }

        logger.trace("Setting user's account active and clearing activation code...");
        user.setActive(true);
        user.setActivationCode(null);

        logger.trace("Saving activated user in database...");
        userRepository.save(user);

        return true;
    }

    public void saveUser(User user, Map<String, String> form) {
    	logger.trace("Updating user's account...");
    	
		user.setFirstName(form.get("firstName"));
		user.setLastName(form.get("lastName"));
		user.setEmail(form.get("email"));
		
		if (form.keySet().contains("active")) {
			user.setActive(true);
		} else {
			user.setActive(false);
		}
		
		user.getAccessLevels().clear();
		
		Set<String> accessLevels = Arrays.stream(AccessLevel.values()).map(AccessLevel::name).collect(Collectors.toSet());

		for (String key : form.keySet()) {
			if (accessLevels.contains(key)) {
				user.getAccessLevels().add(AccessLevel.valueOf(key));
			}
		}
		
		logger.trace("Saving updated user in database...");
		userRepository.save(user);
	}
    
	public void updateProfile(User user, String firstName, String lastName, String email, String password,
			String birthDate, String city, String school, MultipartFile photo, String removePhotoFlag) throws IOException {
		logger.trace("Updating user's profile...");
		
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPassword(passwordEncoder.encode(password));

		logger.trace("Checking user's email for being changed...");
		String userEmail = user.getEmail();
		boolean isEmailChanged = (email != null && !email.equals(userEmail))
				|| (userEmail != null && !userEmail.equals(email));

		if (isEmailChanged) {
			user.setEmail(email);
			user.setActive(false);
			user.setActivationCode(UUID.randomUUID().toString());
			sendActivationCode(user);
		}
		
		if (user.getAccessLevels().contains(AccessLevel.valueOf("USER"))) {
			logger.trace("Updating applicant's profile...");
			Optional<Applicant> applicantFromDb = applicantRepository.findById(user.getId());
			Applicant applicant = applicantFromDb.orElse(new Applicant());
			
			applicant.setId(user.getId());
			
			if (!birthDate.isEmpty()) {
				applicant.setBirthDate(LocalDate.parse(birthDate));
			}
			
			if (!city.isEmpty()) {
				applicant.setCity(city);
			}
			
			if (!school.isEmpty()) {
				applicant.setSchool(school);
			}
			
			boolean removePhoto = (removePhotoFlag == null) ? false : true;
			if (!photo.isEmpty() || !applicantFromDb.isPresent() || removePhoto) {
				applicant.setFileName(StringUtils.cleanPath(photo.getOriginalFilename()));
				applicant.setFileType(photo.getContentType());
				applicant.setFileData(photo.getBytes());
			}
			
			applicant.setUser(user);
			user.setApplicant(applicant);
		}
		
		logger.trace("Saving updated user's profile in database...");
		userRepository.save(user);
	}
	
	public String parseFileData(User user) throws UnsupportedEncodingException {
		logger.trace("Parsing applicant's profile image from byte and mapping to Base64 encoding...");
		
		String fileBase64Encoded = new String();
		
		if (user.getApplicant() != null) {
			byte[] fileBytes = user.getApplicant().getFileData();
			byte[] fileEncodeBase64 = Base64.encodeBase64(fileBytes);
			fileBase64Encoded = new String(fileEncodeBase64, "UTF-8");
		}
		
		return fileBase64Encoded;
	}
}
