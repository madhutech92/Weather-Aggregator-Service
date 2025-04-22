package com.maddy.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    
    public WeatherController(WeatherService weatherService) {
		super();
		this.weatherService = weatherService;
	}


	@GetMapping
    public Mono<ResponseEntity<WeatherResponse>> getWeather(@RequestParam String city) {
        return weatherService.getAggregatedWeather(city)
            .map(ResponseEntity::ok)
            .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new WeatherResponse("Error: " + ex.getMessage(), 0.0))));
    }
}
