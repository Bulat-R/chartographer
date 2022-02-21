package com.example.chartographer;

import com.example.chartographer.exception.NotFoundException;
import com.example.chartographer.service.ChartaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class ChartaControllerTest {

    @MockBean
    private ChartaService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void create_return_201_and_id() throws Exception {

        Mockito.when(service.create(Mockito.anyInt(), Mockito.anyInt())).thenReturn("file id");

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas")
                        .param("width", "20000")
                        .param("height", "50000")
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("file id"));

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas")
                        .param("width", "1")
                        .param("height", "1")
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("file id"));
    }

    @Test
    public void create_return_400_when_wrong_params() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas")
                        .param("width", "1")
                        .param("height", "0")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas")
                        .param("width", "1")
                        .param("height", "50001")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas")                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void save_return_200() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas/1")
                        .param("width", "150")
                        .param("height", "150")
                        .param("x", "0")
                        .param("y", "0")
                        .contentType("image/bmp")
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void save_return_404_when_wrong_id() throws Exception {

        Mockito.doThrow(new NotFoundException("id not found")).when(service).checkId(Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas/1")
                        .param("width", "150")
                        .param("height", "150")
                        .param("x", "0")
                        .param("y", "0")
                        .contentType("image/bmp")
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void save_return_400_when_wrong_params() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas/1")
                        .param("width", "-98")
                        .param("height", "150")
                        .param("x", "0")
                        .param("y", "0")
                        .contentType("image/bmp")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/chartas/1")
                        .param("width", "200")
                        .param("height", "150")
                        .param("x", "dfg6")
                        .param("y", "0")
                        .contentType("image/bmp")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void get_return_200_and_byteArray() throws Exception {

        ByteArrayResource resource = new ByteArrayResource(new byte[]{25, 25, 15, 120});
        Mockito.when(service.get(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(resource);

        mockMvc.perform(MockMvcRequestBuilders.get("/chartas/{id}", 10)
                        .param("width", "200")
                        .param("height", "150")
                        .param("x", "0")
                        .param("y", "0")
                )
                .andExpect(MockMvcResultMatchers.content().contentType("image/bmp"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(resource.getByteArray()));
    }

    @Test
    public void get_return_404_when_wrong_id() throws Exception {

        Mockito.doThrow(new NotFoundException("id not found")).when(service).checkId(Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.get("/chartas/{id}", 10)
                .param("width", "200")
                .param("height", "150")
                .param("x", "0")
                .param("y", "0")
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void get_return_400_when_wrong_params() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/chartas/{id}", 10)
                        .param("width", "5001")
                        .param("height", "150")
                        .param("x", "0")
                        .param("y", "0")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.get("/chartas/{id}", 10)
                        .param("width", "200")
                        .param("height", "150")
                        .param("x", "-54")
                        .param("y", "0")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void delete_return_200() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/chartas/{id}", 10))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void delete_return_404_when_wrong_id() throws Exception {

        Mockito.doThrow(new NotFoundException("id not found")).when(service).checkId(Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.delete("/chartas/{id}", 10))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
