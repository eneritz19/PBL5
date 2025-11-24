package edu.mondragon.we2.crud_rest_db.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.mondragon.we2.crud_rest_db.entity.User;
import edu.mondragon.we2.crud_rest_db.repository.UserRepository;
import jakarta.websocket.server.PathParam;

@RestController
@RequestMapping("/skinXpert")
public class UserController {

    @Autowired
    UserRepository user_repository;

    /**
     * @brief This method returns the list of articles in XML and JSON format.
     * @return an HTTP response (OK if there are articles in the database, not found
     *         if there are
     *         not articles)
     */

    
    @GetMapping(value = "/show", produces = { "application/json", "application/xml" })
    @ResponseBody
    public ResponseEntity<List<User>>getDisease() {

        List<User> user_list = user_repository.findAll();

        if (user_list.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(user_list, HttpStatus.OK);
        }

    }
 
    /**
     * @brief This method returns the information about an article in XML and JSON
     *        formats.
     * @param id identifier of the article as query param.
     * @return an HTTP response (OK if the article is found, not found if the
     *         article does not exist in the database)
     */
    /*@GetMapping(value = "/cityWeather/{id}", produces = { "application/json", "application/xml" })
    public ResponseEntity<Disease>getCityWeather(@PathVariable int id) {

        Optional<Disease> article = city_repository.findById(id);

        if (article.isPresent()) {
            return new ResponseEntity<>(article.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }

    }*/

    //postman
    // POST http://localhost:8080/weatherservice/addCityWeather
    // RAW
    /*{
    "idCity": 5,
    "cityName": "GALICIA",
    "maxTemperature": 0,
    "minTemperature": 0,
    "weather": 19,
    "windSpeed": 0,
    "rain": 0.0
    } */
    /*@PostMapping(value = "/addCityWeather", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Disease> addCityWeather(@RequestBody Disease article) {

       
            city_repository.save(article);
            return new ResponseEntity<>(article, HttpStatus.CREATED);
       

    }*/

    // PUT  http://localhost:8080/weatherservice/modifyCity/1
    /* {
    "idCity": 1,
    "cityName": "GALICIA",
    "maxTemperature": 0,
    "minTemperature": 0,
    "weather": 19,
    "windSpeed": 0,
    "rain": 0.0
     }*/
    /*@PutMapping(value = "/modifyCity/{id}", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Disease> putArticle(@PathVariable int id, @RequestBody Disease article) {

        Optional<Disease> found_article = city_repository.findById(id);

        if (found_article.isPresent()) {

            found_article.get().setCityName(article.getCityName());
            found_article.get().setIdCity(article.getIdCity());
            found_article.get().setMaxTemperature(article.getMaxTemperature());
            found_article.get().setMinTemperature(article.getMinTemperature());
            found_article.get().setRain(article.getRain());
            found_article.get().setWeather(article.getWeather());
            found_article.get().setWindSpeed(article.getWindSpeed());
            city_repository.save(found_article.get());
            city_repository.save(found_article.get());
            return new ResponseEntity<>(article, HttpStatus.OK);

        } else {
            return ResponseEntity.notFound().build();
        }

    }*/



    // DELETE http://localhost:8080/weatherservice/deleteCityWeather?id=5

    /*@DeleteMapping(value = "/deleteCityWeather")  //CAMBIAR A PARAM
    public ResponseEntity<Disease> deleteCityWeather(@RequestParam int id) {

        Optional<Disease> found_article = city_repository.findById(id);

        if (found_article.isPresent()) {

            city_repository.delete(found_article.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }

    }
*/
 

}
