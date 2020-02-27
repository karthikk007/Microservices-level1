package com.kk.moviecatalogservice.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

import com.kk.moviecatalogservice.models.Movie;
import com.kk.moviecatalogservice.models.UserRating;
import com.netflix.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kk.moviecatalogservice.models.CatalogItem;
import com.kk.moviecatalogservice.models.Rating;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private WebClient.Builder webClientBuilder;

	@RequestMapping("/{userId}")
	public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {

		WebClient.Builder builder = WebClient.builder();

		UserRating ratings = restTemplate.getForObject("http://rating-data-service/ratings/users/" + userId, UserRating.class);

		return ratings.getUserRatings().stream().map(rating -> {
			Movie movie = webClientBuilder.build()
					.get()
					.uri("http://movie-info-service/movies/" + rating.getMovieId())
					.retrieve()
					.bodyToMono(Movie.class)
					.block();

			return new CatalogItem(movie.getName(), movie.getName(), rating.getRating());
		}).collect(Collectors.toList());
		
	}
}
