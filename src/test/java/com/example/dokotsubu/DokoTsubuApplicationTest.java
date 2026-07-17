package com.example.dokotsubu;

import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:springBootContext;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DokoTsubuApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Flyway flyway;

    @Test
    void applicationContextStarts() {
    }

    @Test
    void rootRedirectsToReactHostedLoginPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index.jsp"));
    }

    @Test
    void flywayMigratesSchemaToVersionOne() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
        assertThat(flyway.info().pending()).isEmpty();
    }
}
