package com.transmaqsur.sigem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Control de acceso por permisos de módulo y API GPS. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTest {

    @Autowired
    MockMvc mvc;

    @Test
    void usuarioAnonimoEsRedirigidoAlLogin() throws Exception {
        mvc.perform(get("/unidades")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void administradorIniciaSesion() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("admin123")).andExpect(authenticated());
    }

    @Test
    @WithMockUser(authorities = {"FLOTA_VER"})
    void permisoVerPermiteConsultarPeroNoRegistrar() throws Exception {
        mvc.perform(get("/unidades")).andExpect(status().isOk());
        mvc.perform(get("/unidades/nuevo")).andExpect(status().isForbidden());
        mvc.perform(post("/unidades/guardar").with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get("/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"FLOTA_VER", "FLOTA_GESTIONAR"})
    void permisoGestionarPermiteElFormulario() throws Exception {
        mvc.perform(get("/unidades/nuevo")).andExpect(status().isOk());
    }

    @Test
    void apiGpsExigeClaveDeDispositivo() throws Exception {
        String json = "{\"codigoUnidad\":\"NO-EXISTE\",\"latitud\":-16.4,\"longitud\":-71.5}";
        mvc.perform(post("/api/gps").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/gps").header("X-API-KEY", "transmaq-gps-2026").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }
}
