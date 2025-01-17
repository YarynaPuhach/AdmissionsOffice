package ua.lviv.lgs.admissionsOffice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.lgs.admissionsOffice.service.UserService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
    private UserService userService;
	@Autowired
    private PasswordEncoder passwordEncoder;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/", "/index", "/registration", "/activate/*").permitAll()
				.anyRequest().authenticated()
			.and()
				.formLogin()
				.loginPage("/login")
				.permitAll()
			.and()
				.logout()
				.deleteCookies("JSESSIONID")
				.permitAll()
			.and()
                .rememberMe()
                .key("uniqueAndSecret")
                .tokenValiditySeconds(3600)
			.and()
				.exceptionHandling()
				.accessDeniedPage("/403")
			.and()
				.csrf();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService)
        		.passwordEncoder(passwordEncoder);
	}

}
