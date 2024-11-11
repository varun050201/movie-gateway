package com.vk.gateway.controller;


import com.vk.gateway.model.Movie;
import com.vk.gateway.model.MovieRating;
import com.vk.gateway.model.Rating;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${movie-service.url}")
    private String movieServiceUrl;

    @Value("${ratings-service.url}")
    private String ratingsServiceUrl;

    @PostMapping
    public ResponseEntity<Object> addMovie(@RequestBody Movie movie) {
        try {
            log.info("Adding movie");
            Movie savedMovie = restTemplate.postForObject(movieServiceUrl, movie, Movie.class);
            return ResponseEntity.ok().body(savedMovie);
        } catch (HttpStatusCodeException ex) {
            log.error("Error adding movie: {}", ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        try {
            log.info("Updating movie: {}", id);
            restTemplate.put(movieServiceUrl + "/" + id, movie);
            return ResponseEntity.ok().build();
        } catch (HttpStatusCodeException ex) {
            log.error("Error updating movie: {}", ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> fetchMovieAndRating(@PathVariable Long id) {

        Movie movie;

        try {
            movie = restTemplate.getForObject(movieServiceUrl + "/" + id, Movie.class);
        } catch (HttpStatusCodeException ex) {
            log.error("Error fetching movie: {}", ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }

        Rating rating;
        try {
            rating = restTemplate.getForObject(ratingsServiceUrl + "/" + movie.getName(), Rating.class);
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                rating = new Rating(null, movie.getName(), 0.0, 0);
            } else {
                rating = new Rating(null, movie.getName(), -1.0, -1);
            }
        } catch (ResourceAccessException ex){
            log.warn("Exception: ", ex.getMessage());
            rating = new Rating(null, movie.getName(), -1.0, -1);
        }


        MovieRating movieRating = new MovieRating();
        movieRating.setMovie(movie);
        movieRating.setRating(rating);

        return ResponseEntity.ok(movieRating);
    }
}