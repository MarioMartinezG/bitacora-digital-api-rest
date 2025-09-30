package com.diginexa.bitacora.integration;

import com.diginexa.bitacora.BitacoraDigitalApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BitacoraDigitalApplication.class)
@AutoConfigureMockMvc
class MenuControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnMenuForRoleEstudiante() throws Exception {
        // Aquí asumo que en tu DB de pruebas tienes un rol con id = 1 (estudiante)
        mockMvc.perform(get("/api/menu/{role}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Validamos que viene un array con al menos un menú
                .andExpect(jsonPath("$[0].label").value("Inicio"))
                .andExpect(jsonPath("$[0].items[0].label").value("Dashboard"))
                .andExpect(jsonPath("$[1].label").value("Bitácora Digital"))
                .andExpect(jsonPath("$[1].items[0].label").value("Caracteriza tu Asignatura"));
    }
}
