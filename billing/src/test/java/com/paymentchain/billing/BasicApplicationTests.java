package com.paymentchain.billing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentchain.billing.common.InvoiceRequestMapper;
import com.paymentchain.billing.common.InvoiceResposeMapper;
import com.paymentchain.billing.controller.InvoiceRestController;
import com.paymentchain.billing.dto.InvoiceRequest;
import com.paymentchain.billing.dto.InvoiceResponse;
import com.paymentchain.billing.entities.Invoice;
import com.paymentchain.billing.respository.InvoiceRepository;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceRestController.class)//junit5 suport extension interface hrough which classes can integrate with the JUnit test.
@AutoConfigureMockMvc /*allow test only http incoming request layer without start the serve,  but starting the full spring application context*/
public class BasicApplicationTests {

    @Autowired
    private MockMvc mockMvc;
    @MockBean 
    private InvoiceRepository ir;
    @MockBean 
    InvoiceRequestMapper irm;
    @MockBean 
    InvoiceResposeMapper irspm;
    private static final String PASSWORD = "admin";
    private static final String USER = "admin";

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreate() throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();
        String encoding = encoder.encodeToString((USER + ":" + PASSWORD).getBytes());
        Invoice mockdto = new Invoice();
        Mockito.when(ir.save(mockdto)).thenReturn(mockdto);
        Mockito.when(irm.InvoiceRequestToInvoice(new InvoiceRequest())).thenReturn(mockdto);
        Mockito.when(irspm.InvoiceToInvoiceRespose(mockdto)).thenReturn(new InvoiceResponse());
        this.mockMvc.perform(post("/billing").header("Authorization", "Basic " + encoding)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(mockdto))
        ).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testFindById() throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();
        String encoding = encoder.encodeToString((USER + ":" + PASSWORD).getBytes());
        Invoice mockdto = new Invoice();
        mockdto.setId(1);
        Mockito.when(ir.findById(mockdto.getId())).thenReturn(Optional.of(mockdto));
        Mockito.when(irm.InvoiceRequestToInvoice(new InvoiceRequest())).thenReturn(mockdto);
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setInvoiceId(1);
        Mockito.when(irspm.InvoiceToInvoiceRespose(mockdto)).thenReturn(invoiceResponse);
        this.mockMvc.perform(get("/billing/{id}", mockdto.getId()).header("Authorization", "Basic " + encoding)
                .accept(MediaType.APPLICATION_JSON)               
        ).andDo(print()).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.invoiceId").value(1));
    }

}
