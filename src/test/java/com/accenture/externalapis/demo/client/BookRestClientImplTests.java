package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.client.RestClient;


import static org.junit.jupiter.api.Assertions.*;

@Description("BookRestClientImpl")
public class BookRestClientImplTests {
    private BookRestClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        client = new BookRestClientImpl(builder, new ExternalServiceProperties("https://external-api.acnbootcamp.lv/api")); // adjust constructor
    }

    @ParameterizedTest(name = "chaos book id {0} throws ClientException")
    @ValueSource(longs = {99, 991, 992, 993, 995, 997, 998, 999})
    void shouldThrowClientException(long id) {
        //that test has been used for validating which chaos books handles as exception.
        //Most of them is handled but i not understand that should be an error on record with id 994 and 996
        //Please add comments in the feedback. In advice thank you
        assertThrows(ClientException.class, () -> client.getBook(id));
    }

    @Test
    @Description("chaos book with id 994 which should made response to many request after multiple request")
    void shouldThrowClientExceptionWithToManyRequest() {
        assertThrows(ClientException.class, () -> {
            for (int i = 0; i < 20; i++)
                client.getBook((long) 994);
        });
    }


}
