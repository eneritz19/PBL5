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

    
    @GetMapping(value = "/showUser", produces = { "application/json", "application/xml" })
    @ResponseBody
    public ResponseEntity<List<User>>getUser() {

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
    @GetMapping(value = "/showUser/{id}", produces = { "application/json", "application/xml" })
    public ResponseEntity<User>getUserID(@PathVariable int id) {

        Optional<User> user = user_repository.findById(id);

        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping(value = "/addUser", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<User> addUser(@RequestBody User user) {

       
            user_repository.save(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
       

    }

    @PutMapping(value = "/modifyUser/{id}", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<User> putUser(@PathVariable int id, @RequestBody User user) {

        Optional<User> found_user = user_repository.findById(id);

        if (found_user.isPresent()) {

            found_user.get().setName(user.getName());
            found_user.get().setEmail(user.getEmail());
            found_user.get().setPassword(user.getPassword());
            user_repository.save(found_user.get());
            return new ResponseEntity<>(user, HttpStatus.OK);

        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @DeleteMapping(value = "/deleteUser") 
    public ResponseEntity<User> deleteUser(@RequestParam int id) {

        Optional<User> found_user = user_repository.findById(id);

        if (found_user.isPresent()) {

            user_repository.delete(found_user.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
