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

import edu.mondragon.we2.crud_rest_db.entity.Request;
import edu.mondragon.we2.crud_rest_db.repository.RequestRepository;

@RestController
@RequestMapping("/skinXpert")
public class RequestController {

    @Autowired
    RequestRepository request_repository;

    /**
     * @brief This method returns the list of articles in XML and JSON format.
     * @return an HTTP response (OK if there are articles in the database, not found
     *         if there are
     *         not articles)
     */

    
    @GetMapping(value = "/showRequest", produces = { "application/json", "application/xml" })
    @ResponseBody
    public ResponseEntity<List<Request>>getRequest() {

        List<Request> request_list = request_repository.findAll();

        if (request_list.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(request_list, HttpStatus.OK);
        }

    }
 
    /**
     * @brief This method returns the information about an article in XML and JSON
     *        formats.
     * @param id identifier of the article as query param.
     * @return an HTTP response (OK if the article is found, not found if the
     *         article does not exist in the database)
     */
    @GetMapping(value = "/showRequest/{id}", produces = { "application/json", "application/xml" })
    public ResponseEntity<Request>getRequestID(@PathVariable int id) {

        Optional<Request> request = request_repository.findById(id);

        if (request.isPresent()) {
            return new ResponseEntity<>(request.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping(value = "/addRequest", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Request> addRequest(@RequestBody Request request) {

       
            request_repository.save(request);
            return new ResponseEntity<>(request, HttpStatus.CREATED);
       

    }

    @PutMapping(value = "/modifyRequest/{id}", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Request> putRequest(@PathVariable int id, @RequestBody Request request) {

        Optional<Request> found_request = request_repository.findById(id);

        if (found_request.isPresent()) {

            found_request.get().setUpload_date(request.getUpload_date());;
            found_request.get().setStatus(request.getStatus());
            request_repository.save(found_request.get());
            request_repository.save(found_request.get());
            return new ResponseEntity<>(request, HttpStatus.OK);

        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @DeleteMapping(value = "/deleteRequest") 
    public ResponseEntity<Request> deleteRequest(@RequestParam int id) {

        Optional<Request> found_request = request_repository.findById(id);

        if (found_request.isPresent()) {

            request_repository.delete(found_request.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
