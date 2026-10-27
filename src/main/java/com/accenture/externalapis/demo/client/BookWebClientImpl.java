package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookApiResponse;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public class BookWebClientImpl implements BookWebClient {

    private WebClient webClient;

    public BookWebClientImpl(WebClient.Builder builder, ExternalServiceProperties properties) {

        this.webClient = builder.baseUrl(properties.baseUrl())
//                .defaultHeader("Authorization", "Bearer " + token")
                .build();
    }

    public Mono<BookDto> getBookAsync(Long id) {

        //that test has been used for validating which chaos books handles as exception.
        //Most of them is handled but i not understand that should be an error on record with id 994 and 996
        //understood 994, it's to many request and it handles and handled correctly too.
        //Please add comments in the feedback. In advice thank you

        //request for single value response
        Mono<BookApiResponse> response = webClient.get()
                .uri("/books/{id}", id)
                .retrieve()
                .bodyToMono(BookApiResponse.class)
                .timeout(Duration.ofSeconds(5))
                .retry(3)
                .onErrorResume(throwable -> Mono.error(new ClientException("After 3 retries error was occurred: ", throwable)))
                .switchIfEmpty(Mono.error(() -> new ClientException("The used method has response with empty body")));
        //if response body is null

        return response.map(this::loadBookDTO);
    }

    public Flux<BookDto> getAllBooksAsync() {

        //that test has been used for validating which chaos books handles as exception.
        //Most of them is handled but i not understand that should be an error on record with id 994 and 996
        //understood 994, it's to many request and it handles and handled correctly too.
        //Please add comments in the feedback. In advice thank you

        //request for multiple values response
        Flux<BookApiResponse> response = webClient.get()
                .uri("/books")
                .retrieve()
                .bodyToFlux(BookApiResponse.class)
                .timeout(Duration.ofSeconds(5))
                .retry(3)
                .onErrorResume(throwable -> Mono.error(new ClientException("After 3 retries error was occurred: ", throwable)))
                .switchIfEmpty(Mono.error(() -> new ClientException("The used method has response with empty body")));

        return response.map(this::loadBookDTO);
    }

    public Mono<List<BookDto>> getBooksInParallel(Long id1, Long id2) {
        //get 2 different result
        Mono<BookDto> book1 = getBookAsync(id1);
        Mono<BookDto> book2 = getBookAsync(id2);

        //combine 2 results
        return Mono.zip(book1, book2)
                .map(tuple -> List.of(
                        tuple.getT1(),
                        tuple.getT2()
                ));
    }

    private BookDto loadBookDTO(BookApiResponse responseRecord) {
        if (responseRecord.price() == null)
            throw new ClientException("The price of the received book is not set");

        return new BookDto(
                responseRecord.title(),
                responseRecord.author(),
                responseRecord.genre(),
                responseRecord.price().doubleValue()
        );
    }
}
