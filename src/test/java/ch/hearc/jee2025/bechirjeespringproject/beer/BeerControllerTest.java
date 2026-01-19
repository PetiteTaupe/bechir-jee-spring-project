package ch.hearc.jee2025.bechirjeespringproject.beer;

import ch.hearc.jee2025.bechirjeespringproject.AdminUtils;
import ch.hearc.jee2025.bechirjeespringproject.brewery.Brewery;
import ch.hearc.jee2025.bechirjeespringproject.brewery.BreweryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
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

class BeerControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private BeerService beerService;
    private BreweryService breweryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        beerService = mock(BeerService.class);
        breweryService = mock(BreweryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new BeerController(beerService, breweryService)).build();
    }

    @Test
    // Vérifie que GET /beers retourne 200 et une liste JSON.
    void getAll_returns200() throws Exception {
        Brewery b = new Brewery("Brew", "CH");
        b.setId(1L);

        Beer beer1 = new Beer("A", 1.0, 1);
        beer1.setId(10L);
        beer1.setBrewery(b);
        Beer beer2 = new Beer("B", 2.0, 2);
        beer2.setId(11L);
        beer2.setBrewery(b);

        when(beerService.findAll()).thenReturn(List.of(beer1, beer2));

        mockMvc.perform(get("/beers"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].name", is("A")));

        verify(beerService).findAll();
        verifyNoMoreInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que GET /beers/{id} retourne 200 quand la bière existe.
    void getById_whenFound_returns200() throws Exception {
        Brewery b = new Brewery("Brew", "CH");
        b.setId(1L);

        Beer beer = new Beer("A", 1.0, 1);
        beer.setId(10L);
        beer.setBrewery(b);

        when(beerService.findById(10L)).thenReturn(Optional.of(beer));

        mockMvc.perform(get("/beers/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("A")))
                .andExpect(jsonPath("$.price", is(1.0)))
                .andExpect(jsonPath("$.stock", is(1)));

        verify(beerService).findById(10L);
        verifyNoMoreInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que GET /beers/{id} retourne 404 quand la bière n'existe pas.
    void getById_whenMissing_returns404() throws Exception {
        when(beerService.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/beers/10"))
                .andExpect(status().isNotFound());

        verify(beerService).findById(10L);
        verifyNoMoreInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que POST /beers sans clé admin retourne 403.
    void create_withoutAdminKey_returns403() throws Exception {
        Beer beer = new Beer("A", 1.0, 1);

        mockMvc.perform(post("/beers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que POST /beers retourne 400 si brewery.id est manquant.
    void create_missingBreweryId_returns400() throws Exception {
        Brewery brewery = new Brewery("B", "CH");
        Beer beer = new Beer("A", 1.0, 1);
        beer.setBrewery(brewery);

        mockMvc.perform(post("/beers")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(beerService);
        verifyNoInteractions(breweryService);
    }

    @Test
    // Vérifie que POST /beers attache une brewery managée et sauvegarde.
    void create_withValidAdminKey_attachesManagedBrewery_andSaves() throws Exception {
        Brewery incoming = new Brewery("B", "CH");
        incoming.setId(5L);

        Brewery managed = new Brewery("B", "CH");
        managed.setId(5L);

        Beer beer = new Beer("A", 1.0, 1);
        beer.setBrewery(incoming);

        when(breweryService.findById(5L)).thenReturn(Optional.of(managed));
        when(beerService.save(any(Beer.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/beers")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isOk());

        ArgumentCaptor<Beer> captor = ArgumentCaptor.forClass(Beer.class);
        verify(breweryService).findById(5L);
        verify(beerService).save(captor.capture());
        assertSame(managed, captor.getValue().getBrewery());
        verifyNoMoreInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que PUT /beers/{id} retourne 404 si la bière n'existe pas.
    void update_whenMissing_returns404() throws Exception {
        when(beerService.findById(10L)).thenReturn(Optional.empty());

        Beer beer = new Beer("A", 1.0, 1);

        mockMvc.perform(put("/beers/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNotFound());

        verify(beerService).findById(10L);
        verifyNoMoreInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que PUT /beers/{id} force l'id, attache la brewery managée et sauvegarde.
    void update_whenFound_setsId_attachesManagedBrewery_andSaves() throws Exception {
        Brewery incoming = new Brewery("B", "CH");
        incoming.setId(5L);

        Brewery managed = new Brewery("B", "CH");
        managed.setId(5L);

        Beer existing = new Beer("Existing", 9.9, 9);
        existing.setId(10L);
        existing.setBrewery(managed);

        when(beerService.findById(10L)).thenReturn(Optional.of(existing));
        when(breweryService.findById(5L)).thenReturn(Optional.of(managed));
        when(beerService.save(any(Beer.class))).thenAnswer(inv -> inv.getArgument(0));

        Beer payload = new Beer("Updated", 1.0, 1);
        payload.setBrewery(incoming);

        mockMvc.perform(put("/beers/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        ArgumentCaptor<Beer> captor = ArgumentCaptor.forClass(Beer.class);
        verify(beerService).findById(10L);
        verify(breweryService).findById(5L);
        verify(beerService).save(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertSame(managed, captor.getValue().getBrewery());
        verifyNoMoreInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que DELETE /beers/{id} sans clé admin retourne 403.
    void delete_withoutAdminKey_returns403() throws Exception {
        mockMvc.perform(delete("/beers/10"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que DELETE /beers/{id} retourne 404 si la bière n'existe pas.
    void delete_whenNotFound_returns404() throws Exception {
        doThrow(new RuntimeException("Beer not found with id: 10")).when(beerService).deleteById(10L);

        mockMvc.perform(delete("/beers/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY))
                .andExpect(status().isNotFound());

        verify(beerService).deleteById(10L);
        verifyNoMoreInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que DELETE /beers/{id} retourne 409 si contrainte d'intégrité (utilisée dans un panier).
    void delete_whenIntegrityViolation_returns409() throws Exception {
        doThrow(new DataIntegrityViolationException("constraint")).when(beerService).deleteById(10L);

        mockMvc.perform(delete("/beers/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY))
                .andExpect(status().isConflict());

        verify(beerService).deleteById(10L);
        verifyNoMoreInteractions(beerService, breweryService);
    }

    @Test
    // Vérifie que DELETE /beers/{id} retourne 200 et un message de succès.
    void delete_whenOk_returns200AndMessage() throws Exception {
        mockMvc.perform(delete("/beers/10")
                        .header("X-ADMIN-KEY", AdminUtils.ADMIN_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Beer deleted successfully")))
                .andExpect(jsonPath("$.id", is("10")));

        verify(beerService).deleteById(10L);
        verifyNoMoreInteractions(beerService, breweryService);
    }
}
