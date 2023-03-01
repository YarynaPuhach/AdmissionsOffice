package ua.lviv.lgs.admissionsOffice;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
public class LoginTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	public void indexPageTest() throws Exception {
		this.mockMvc.perform(get("/"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Welcome!")));
	}
	
	@Test
	public void notAuthenticatedTest() throws Exception {
		this.mockMvc.perform(get("/main"))
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	public void correctLoginTest() throws Exception {
		this.mockMvc.perform(formLogin().user("admissions.office.info@gmail.com").password("admin"))
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"));
	}
	
	@Test
	public void incorrectLoginTest() throws Exception {
		this.mockMvc.perform(formLogin().user("admissions.office.info@gmail.com").password(""))
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error"));
	}
	
	@Test
	public void badCredentialsTest() throws Exception {
		this.mockMvc.perform(post("/login").param("user", "Vasya"))
			.andDo(print())
			.andExpect(status().isForbidden());			
	}
}
