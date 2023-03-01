package ua.lviv.lgs.admissionsOffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import ua.lviv.lgs.admissionsOffice.controller.MainController;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
public class MainControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MainController mainController;

	@Test
	public void mainControllerTest() throws Exception {
		assertThat(mainController).isNotNull();
	}

	@Test
	@WithUserDetails("admissions.office.info@gmail.com")
	public void mainPageResponseTest() throws Exception {
		this.mockMvc.perform(get("/main"))
			.andDo(print())
			.andExpect(authenticated())
			.andExpect(xpath("//*[@id='navbarSupportedContent']/div[2]/a")
					.string(containsString("Admissions office")));
	}

	@Test
	@WithUserDetails("admissions.office.info@gmail.com")
	@Sql(value = {"/sql/createApplication.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = {"/sql/dropApplication.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	public void notAcceptedAppsListTest() throws Exception {
		this.mockMvc.perform(get("/main"))
			.andDo(print())
			.andExpect(authenticated())
			.andExpect(xpath("//*[@id='notAcceptedApps']/div").nodeCount(3));
	}
	
	@Test
	@WithUserDetails("ivanov@mail.com")
	@Sql(value = {"/sql/createApplication.sql", "/sql/acceptApplication.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = {"/sql/dropApplication.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	public void admittedSpecialitiesListTest() throws Exception {
		this.mockMvc.perform(get("/main"))
			.andDo(print())
			.andExpect(authenticated())			
			.andExpect(xpath("//*[@id='admittedSpecialities']/div").nodeCount(2));
	}
	
	@Test
	@WithUserDetails("ivanov@mail.com")
	@Sql(value = {"/sql/createApplication.sql", "/sql/rejectApplication.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = {"/sql/dropApplication.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	public void rejectedAppExistsTest() throws Exception {
		this.mockMvc.perform(get("/main"))
			.andDo(print())
			.andExpect(authenticated())
			.andExpect(content().string(containsString("Some previously submitted applications were rejected")));			
	}
}
