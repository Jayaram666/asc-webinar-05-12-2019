package com.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Log4j2
@SpringBootApplication
public class SimpleMicroserviceApplication {

  @Bean
  RouterFunction<ServerResponse> routes(@Value("${spring.data.mongodb.uri:}") String mongoDbUri,
      @Value("${application.message}") String message, CustomerRepository customerRepository) {

    log.info("mongoDbUri: " + mongoDbUri);

    return route().GET("/customers", req -> {
      log.info("returning all the " + Customer.class.getName() + " instances.");
      return ok().body(customerRepository.findAll(), Customer.class);
    }).GET("/hello", r -> ok().bodyValue(Map.of("greeting", message))).build();
  }

  @Bean
  ApplicationListener<ApplicationReadyEvent> ready(CustomerRepository repository) {
    return event -> repository.deleteAll()
        .thenMany(Flux.just("A", "B", "C").map(name -> new Customer(null, name)).flatMap(repository::save))
        .thenMany(repository.findAll()).subscribe(log::info);
  }

  public static void main(String[] args) {
    SpringApplication.run(SimpleMicroserviceApplication.class, args);
  }
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, String> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class Customer {

  @Id
  private String id;
  private String name;
}