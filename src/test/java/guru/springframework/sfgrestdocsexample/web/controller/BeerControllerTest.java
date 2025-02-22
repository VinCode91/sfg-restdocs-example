package guru.springframework.sfgrestdocsexample.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sfgrestdocsexample.domain.Beer;
import guru.springframework.sfgrestdocsexample.repositories.BeerRepository;
import guru.springframework.sfgrestdocsexample.web.model.BeerDto;
import guru.springframework.sfgrestdocsexample.web.model.BeerStyleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.springframework.guru", uriPort = 443)
@WebMvcTest(BeerController.class)
@ComponentScan(basePackages = "guru.springframework.sfgrestdocsexample.web.mappers")
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerRepository beerRepository;


    @Test
    void getBeerById() throws Exception {
        given(beerRepository.findById(any())).willReturn(Optional.of(Beer.builder().build()));

        //MvcResult result =
                mockMvc.perform(get("/api/v1/beer/{beerId}", UUID.randomUUID().toString())
                        .param("iscold", "yes") // Juste pour l'exemple
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("v1/beer-get", // unique identifier for each request so documentation can distinguish
                        pathParameters(
                                parameterWithName("beerId").description("UUID de la bière demandée")
                        ), requestParameters(
                                parameterWithName("iscold").description("Bière froide")
                        ), responseFields(
                                fieldWithPath("id").description("Id de bière").type(UUID.class),
                                fieldWithPath("version").description("numéro de version"),
                                fieldWithPath("createdDate").description("Date où la bière arrive en stock"),
                                fieldWithPath("beerName").description("nom de la bière"),
                                fieldWithPath("beerStyle").description("pression ou non"),
                                fieldWithPath("upc").description("Code barre"),
                                fieldWithPath("price").description("Prix"),
                                fieldWithPath("quantityOnHand").description("reste en stock"),
                                fieldWithPath("lastModifiedDate").description("dernière mise à jour")
                        )
                ));
                //.andReturn();
        //System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void saveNewBeer() throws Exception {
        BeerDto beerDto =  getValidBeerDto();
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

        mockMvc.perform(post("/api/v1/beer/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
                .andExpect(status().isCreated())
                .andDo(document("v1/beer-new", // unique identifier for each request so documentation can distinguish
                        requestFields(
                                fieldWithPath("id").ignored(),
                                fieldWithPath("version").ignored(),
                                fieldWithPath("createdDate").ignored(),
                                fields.withPath("beerName").description("nom de la bière"),
                                fields.withPath("beerStyle").description("type de bière (pression etc...)"),
                                fields.withPath("upc").description("Code barre"),
                                fields.withPath("price").description("Prix"),
                                fieldWithPath("quantityOnHand").ignored(),
                                fieldWithPath("lastModifiedDate").ignored()
                        )
                        ))
        ;
    }

    @Test
    void updateBeerById() throws Exception {
        BeerDto beerDto =  getValidBeerDto();
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        mockMvc.perform(put("/api/v1/beer/" + UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
                .andExpect(status().isNoContent());
    }

    BeerDto getValidBeerDto(){
        return BeerDto.builder()
                .beerName("Nice Ale")
                .beerStyle(BeerStyleEnum.ALE)
                .price(new BigDecimal("9.99"))
                .upc(123123123123L)
                .build();

    }

    private static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }

}
