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

import edu.mondragon.we2.crud_rest_db.entity.Result;
import edu.mondragon.we2.crud_rest_db.repository.ResultRepository;

@RestController
@RequestMapping("/skinXpert")
public class ResultController {

    @Autowired
    ResultRepository result_repository;

    /**
     * @brief This method returns the list of articles in XML and JSON format.
     * @return an HTTP response (OK if there are articles in the database, not found
     *         if there are
     *         not articles)
     */

    
    @GetMapping(value = "/showResult", produces = { "application/json", "application/xml" })
    @ResponseBody
    public ResponseEntity<List<Result>>getResult() {

        List<Result> result_list = result_repository.findAll();

        if (result_list.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(result_list, HttpStatus.OK);
        }

    }
 
    /**
     * @brief This method returns the information about an article in XML and JSON
     *        formats.
     * @param id identifier of the article as query param.
     * @return an HTTP response (OK if the article is found, not found if the
     *         article does not exist in the database)
     */
    @GetMapping(value = "/showResult/{id}", produces = { "application/json", "application/xml" })
    public ResponseEntity<Result>getResultID(@PathVariable int id) {

        Optional<Result> result = result_repository.findById(id);

        if (result.isPresent()) {
            return new ResponseEntity<>(result.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping(value = "/addResult", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Result> addResult(@RequestBody Result result) {

       
            result_repository.save(result);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
       

    }

    @PutMapping(value = "/modifyResult/{id}", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Result> putResult(@PathVariable int id, @RequestBody Result result) {

        Optional<Result> found_result = result_repository.findById(id);

        if (found_result.isPresent()) {

            found_result.get().setDiagnosis(result.getDiagnosis());
            found_result.get().setConfidence_level(result.getConfidence_level());
            found_result.get().setRecommendations(result.getRecommendations());
            found_result.get().setAnalysis_date(result.getAnalysis_date());
            result_repository.save(found_result.get());
            return new ResponseEntity<>(result, HttpStatus.OK);

        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @DeleteMapping(value = "/deleteResult") 
    public ResponseEntity<Result> deleteResult(@RequestParam int id) {

        Optional<Result> found_result = result_repository.findById(id);

        if (found_result.isPresent()) {

            result_repository.delete(found_result.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
