package ch.hearc.jee2025.bechirjeespringproject.brewery;

import ch.hearc.jee2025.bechirjeespringproject.AdminUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BreweryControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private BreweryService breweryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        breweryService = mock(BreweryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new BreweryController(breweryService)).build();
    }

    @Test
    // Vérifie que GET /breweries retourne 200 et une liste JSON.
    void getAll_returns200() throws Exception {
        Brewery b1 = new Brewery("B1", "CH");
        b1.setId(10L);
        Brewery b2 = new Brewery("B2", "FR");
        b2.setId(11L);

        when(breweryService.findAll()).thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/breweries"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].name", is("B1")));

        verify(breweryService).findAll();
        verifyNoMoreInteractions(breweryService);
    }

    @Test
    // Vérifie que GET /breweries/{id} retourne 200 quand la brasserie existe.
    void getById_whenFound_returns200() throws Exception {
        Brewery brewery = new Brewery("B1", "CH");
        brewery.setId(10L);

        when(breweryService.findById(10L)).thenReturn(Optional.of(brewery));

        mockMvc.perform(get("/breweries/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("B1")))
                .andExpect(jsonPath("$.country", is("CH")));

        verify(breweryService).findById(10L);
        verifyNoMoreInteractions(breweryService);
    }

    @Test
    // Vérifie que GET /breweries/{id} retourne 404 quand la brasserie n'existe pas.
    void getById_whenMissing_returns404() throws Exception {
        when(breweryService.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/breweries/10"))
                .andExpect(status().isNotFound());

        verify(breweryService).findById(10L);
        verifyNoMoreInteractions(breweryService);
    }

    @Test
    // Vérifie que POST /breweries sans clé admin retourne 403.
    void create_withoutAdminKey_returns403() throws Exception {
        Brewery brewery = new Brewery("B1", "CH");

        mockMvc.perform(post("/breweries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brewery)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(breweryService);
    }

    @Test
    // Vérifie que POST /breweries sauvegarde une brasserie avec une clé admin valide.
    void create_withValidAdminKey_saves() throws Exception {
        Brewery brewery = new Brewery("B1", "CH");
        when(breweryService.save(any(Brewery.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/breweries")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brewery)))
                .andExpect(status().isOk());

        verify(breweryService).save(any(Brewery.class));
        verifyNoMoreInteractions(breweryService);
    }

    @Test
    // Vérifie que PUT /breweries/{id} retourne 404 si la brasserie n'existe pas.
    void update_whenMissing_returns404() throws Exception {
        when(breweryService.findById(10L)).thenReturn(Optional.empty());

        Brewery payload = new Brewery("B1", "CH");

        mockMvc.perform(put("/breweries/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());

        verify(breweryService).findById(10L);
        verifyNoMoreInteractions(breweryService);
    }

    @Test
    // Vérifie que PUT /breweries/{id} force l'id et sauvegarde avec une clé admin valide.
    void update_whenFound_setsId_andSaves() throws Exception {
        Brewery existing = new Brewery("Old", "CH");
        existing.setId(10L);

        when(breweryService.findById(10L)).thenReturn(Optional.of(existing));
        when(breweryService.save(any(Brewery.class))).thenAnswer(inv -> inv.getArgument(0));

        Brewery payload = new Brewery("New", "FR");

        mockMvc.perform(put("/breweries/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        ArgumentCaptor<Brewery> captor = ArgumentCaptor.forClass(Brewery.class);
        verify(breweryService).findById(10L);
        verify(breweryService).save(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        verifyNoMoreInteractions(breweryService);
    }

    @Test
    // Vérifie que DELETE /breweries/{id} sans clé admin retourne 403.
    void delete_withoutAdminKey_returns403() throws Exception {
        mockMvc.perform(delete("/breweries/10"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(breweryService);
    }

    @Test
    // Vérifie que DELETE /breweries/{id} retourne 404 si la brasserie n'existe pas.
    void delete_whenNotFound_returns404() throws Exception {
        doThrow(new RuntimeException("Brewery not found with id: 10")).when(breweryService).deleteById(10L);

        mockMvc.perform(delete("/breweries/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY))
                .andExpect(status().isNotFound());

        verify(breweryService).deleteById(10L);
        verifyNoMoreInteractions(breweryService);
    }

    @Test
    // Vérifie que DELETE /breweries/{id} retourne 200 et un message de succès.
    void delete_whenOk_returns200AndMessage() throws Exception {
        mockMvc.perform(delete("/breweries/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Brewery deleted successfully")))
                .andExpect(jsonPath("$.id", is("10")));

        verify(breweryService).deleteById(10L);
        verifyNoMoreInteractions(breweryService);
    }
}
