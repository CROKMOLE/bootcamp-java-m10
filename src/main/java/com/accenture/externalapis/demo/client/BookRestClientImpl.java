package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookApiResponse;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.time.Duration;
import java.util.List;

@Component
public class BookRestClientImpl implements BookRestClient {

    private RestClient restClient;

    public BookRestClientImpl(RestClient.Builder builder, ExternalServiceProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));  // max time to establish the connection
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = builder.baseUrl(properties.baseUrl())
                .requestFactory(factory)
//                .defaultHeader("Authorization", "Bearer " + token")
                .build();
        // and assign it to this.restClient
        //
        // Optional/bonus: this service doesn't require auth, but in a real API you would
        // often also add builder.defaultHeader("Authorization", "Bearer " + token) here.
    }


    public BookDto getBook(Long id) {
        try {

            //that test has been used for validating which chaos books handles as exception.
            //Most of them is handled but i not understand that should be an error on record with id 994 and 996
            //understood 994, it's to many request and it handles and handled correctly too.
            //Please add comments in the feedback. In advice thank you

            //requesting single record using external REST API
            BookApiResponse response = restClient.get()
                    .uri("/books/{id}", id)
                    .retrieve()
                    .body(BookApiResponse.class);

            //if response body is null
            if(response == null)
                throw new ClientException("The used method has response with empty body");

            //transform the response book record to DTO
            return loadBookDTO(response);

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ClientException("Record not found", ex);
        } catch (HttpClientErrorException ex) {
            throw new ClientException("Client error: ", ex);
        } catch (HttpServerErrorException ex) {
            throw new ClientException("Server error: ", ex);
        } catch (ResourceAccessException ex) {
            throw new ClientException("Server is not accessible: ", ex);
        } catch (RestClientException ex) {
            throw new ClientException("Request has un-readable response: ", ex);
        }
    }

    public List<BookDto> getAllBooks() {
        try {
            //requesting all records using external REST API
            List<BookApiResponse> response = restClient.get()
                    .uri("/books")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<BookApiResponse>>() {
                    });

            //if response body is null
            if(response == null)
                throw new ClientException("The used method has response with empty body");

            //transform response book records to DTOs list
            return response.stream().map(this::loadBookDTO)
                    .toList();

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ClientException("Record not found", ex);
        } catch (HttpClientErrorException ex) {
            throw new ClientException("Client error: ", ex);
        } catch (HttpServerErrorException ex) {
            throw new ClientException("Server error: ", ex);
        } catch (ResourceAccessException ex) {
            throw new ClientException("Server is not accessible: ", ex);
        }
    }

    /**
     * constructs the dto
     */
    private BookDto loadBookDTO(BookApiResponse responseRecord) {
        if(responseRecord.price() == null)
            throw new ClientException("The price of the received book is not set");

        return new BookDto(
                responseRecord.title(),
                responseRecord.author(),
                responseRecord.genre(),
                responseRecord.price().doubleValue()
        );
    }
}
