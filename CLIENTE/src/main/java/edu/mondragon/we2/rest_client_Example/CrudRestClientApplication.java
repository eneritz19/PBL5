package edu.mondragon.we2.rest_client_Example;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootApplication
public class CrudRestClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrudRestClientApplication.class, args);

		List<City> cities = getCitiesWeather(); //AÑADIR ESTO
	}

	
	            //cambiar esto 
	public static List<City>  getCitiesWeather() 
	{

		List<City> city_list = new ArrayList<City>(); // AÑADIR ESTO
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:8080/weatherservice/citiesWeather";

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<City> request = new HttpEntity<>(null, headers);
		ResponseEntity<List<City>> responseEntity = restTemplate.exchange(
				url,
				HttpMethod.GET,
				request,
				new ParameterizedTypeReference<List<City>>() {
				});

		List<City> cities = responseEntity.getBody();

		for (City city : cities) { //AÑADIR ESTO
			                               //QUITAR EL GET (PORQUE ES UNA LISTA)
		   System.out.println("Id: " + city.getIdCity());
			System.out.println("Name: " + city.getCityName());
			System.out.println("Temperature max: " + city.getMaxTemperature());
			System.out.println("Min temperature: " + city.getMinTemperature());
			System.out.println("Rain: " + city.getRain());
			System.out.println("Weather: " + city.getWeather());
			System.out.println("Wind: " + city.getWindSpeed());

		
		}

		return cities; //añadir esto
	}

	                                 //añadir esto
	public static void addCityWeather(City city) 
	{
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:8080/weatherservice/addCityWeather";
		                                                  //ADD CITY

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<City> request = new HttpEntity<>(null, headers);
                                       //CAMBIAR ESTO
		ResponseEntity<City> response = restTemplate.exchange(url, HttpMethod.POST, request, City.class);

		city = response.getBody();

		if (city != null) {
			System.out.println("Added City:");
			System.out.println("Id: " + city.getIdCity());
			System.out.println("Name: " + city.getCityName());
			System.out.println("Temperature max: " + city.getMaxTemperature());
			System.out.println("Min temperature: " + city.getMinTemperature());
			System.out.println("Rain: " + city.getRain());
			System.out.println("Weather: " + city.getWeather());
			System.out.println("Wind: " + city.getWindSpeed());
		}
	}

	                                 //añadir esto
	public static void addCityWeather(int id) 
	{
		RestTemplate restTemplate = new RestTemplate(); 
		String status = new String(); //añadir                         
		String url = "http://localhost:8080/weatherservice/deleteCityWeather?id=" + id;  
		                                                  //ADD CITY              id

	                  //STRING                 //CAMBIAR ESTO                            null
		ResponseEntity<City> response = restTemplate.exchange(url, HttpMethod.POST, null, City.class);

		//añadir esto
		status = response.getStatusCode().toString(); //MEMORIA
		System.out.println("DELETED! Status: " + status);
		
	}
	

}
