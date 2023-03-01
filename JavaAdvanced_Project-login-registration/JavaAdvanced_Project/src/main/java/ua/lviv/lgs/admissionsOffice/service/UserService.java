package ua.lviv.lgs.admissionsOffice.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ua.lviv.lgs.admissionsOffice.dao.UserRepository;
import ua.lviv.lgs.admissionsOffice.domain.AccessLevel;
import ua.lviv.lgs.admissionsOffice.domain.User;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
	@Autowired
	private MailSender mailSender;
	@Autowired
    private PasswordEncoder passwordEncoder;
	
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email);
    }
    
    public User findById(Integer id) {
    	return userRepository.findById(id).get();
    }
    
    public List<User> findAll() {
    	return userRepository.findAll();
    }

    public boolean addUser(User user) {
        User userFromDb = userRepository.findByEmail(user.getEmail());

        if (userFromDb != null) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false);
        user.setAccessLevels(Collections.singleton(AccessLevel.USER));
        user.setActivationCode(UUID.randomUUID().toString());

        userRepository.save(user);
        sendActivationCode(user);
        return true;
    }

	public void sendActivationCode(User user) {
		if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
            		"Доброго времени суток, %s %s! \n\n" +
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
        User user = userRepository.findByActivationCode(code);

        if (user == null) {
            return false;
        }

        user.setActive(true);
        user.setActivationCode(null);

        userRepository.save(user);

        return true;
    }

    public void saveUser(User user, Map<String, String> form) {
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
	}
    
	public void updateProfile(User user, String firstName, String lastName, String email, String password) {
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPassword(passwordEncoder.encode(password));

		String userEmail = user.getEmail();
		boolean isEmailChanged = (email != null && !email.equals(userEmail))
				|| (userEmail != null && !userEmail.equals(email));

		if (isEmailChanged) {
			user.setEmail(email);
			user.setActive(false);
			user.setActivationCode(UUID.randomUUID().toString());
			sendActivationCode(user);
		}

		userRepository.save(user);
	}
}
