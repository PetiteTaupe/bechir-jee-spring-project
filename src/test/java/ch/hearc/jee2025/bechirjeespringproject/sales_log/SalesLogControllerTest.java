package ch.hearc.jee2025.bechirjeespringproject.sales_log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SalesLogControllerTest {

    private SalesLogService salesLogService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        salesLogService = mock(SalesLogService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SalesLogController(salesLogService)).build();
    }

    @Test
    void getTopBeers_defaultLimit_returnsList() throws Exception {
        when(salesLogService.getTopBeers(10)).thenReturn(List.of(
                new TopBeerSales(1L, "Beer A", 12),
                new TopBeerSales(2L, "Beer B", 5)
        ));

        mockMvc.perform(get("/sales_logs/top"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].beerId", is(1)))
                .andExpect(jsonPath("$[0].beerName", is("Beer A")))
                .andExpect(jsonPath("$[0].totalQuantity", is(12)));

        verify(salesLogService).getTopBeers(10);
        verifyNoMoreInteractions(salesLogService);
    }

    @Test
    void getTopBeers_invalidLimit_returns400() throws Exception {
        mockMvc.perform(get("/sales_logs/top?limit=0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(salesLogService);
    }
}