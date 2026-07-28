package bnds.chinese.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "thunderstorm.data-file=${java.io.tmpdir}/thunderstorm-controller-test/events.json")
class EventControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsEightCharacters() throws Exception {
        mockMvc.perform(get("/api/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[5].name").value("周朴园"));
    }

    @Test
    void rejectsInvalidEvent() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content("""
                                {"name":"","description":"","initiator":null,
                                 "affectedCharacters":[],"affectionDelta":11}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
