package com.maddy.demo;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@Service
public class WeatherService {

    private final WebClient webClient = WebClient.create();
    @Value(value = "${weather.key}")
    private String apiKey;
    
    public WeatherService() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Mono<WeatherResponse> getAggregatedWeather(String city) {
        CompletableFuture<WeatherResponse> source1 = fetchFromSource1(city);
        CompletableFuture<WeatherResponse> source2 = fetchFromSource2(city);

        return Mono.fromFuture(CompletableFuture.allOf(source1, source2)
            .thenApply(v -> {
                try {
                    double avgTemp = (source1.get().temperature() + source2.get().temperature()) / 2;
                    String summary = source1.get().summary() + " / " + source2.get().summary();
                    return new WeatherResponse(summary, avgTemp);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to combine results", e);
                }
            }));
    }

	/*
	 * private CompletableFuture<WeatherResponse> fetchFromSource1(String city) {
	 * return webClient.get() .uri("https://mock-weather-api.com/source1?city=" +
	 * city) .retrieve() .bodyToMono(WeatherResponse.class) .toFuture(); }
	 */

    private CompletableFuture<WeatherResponse> fetchFromSource2(String city) {
        return webClient.get()
                .uri("https://mock-weather-api.com/source2?city=" + city)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .toFuture();
    }
    
    private CompletableFuture<WeatherResponse> fetchFromSource1(String city) {
        return webClient.get()
                .uri("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    String summary = json.get("weather").get(0).get("description").asText();
                    double temp = json.get("main").get("temp").asDouble();
                    return new WeatherResponse(summary, temp);
                })
                .toFuture();
    }
}
