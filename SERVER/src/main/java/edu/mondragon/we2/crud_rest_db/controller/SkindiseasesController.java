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

import edu.mondragon.we2.crud_rest_db.entity.Skindiseases;
import edu.mondragon.we2.crud_rest_db.repository.SkindiseasesRepository;

@RestController
@RequestMapping("/skinXpert")
public class SkindiseasesController {

    @Autowired
    SkindiseasesRepository skin_repository;

    /**
     * @brief This method returns the list of articles in XML and JSON format.
     * @return an HTTP response (OK if there are articles in the database, not found
     *         if there are
     *         not articles)
     */

    
    @GetMapping(value = "/showSkindiseases", produces = { "application/json", "application/xml" })
    @ResponseBody
    public ResponseEntity<List<Skindiseases>>getskindisease() {

        List<Skindiseases> skindisease_list = skin_repository.findAll();

        if (skindisease_list.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(skindisease_list, HttpStatus.OK);
        }

    }
 
    /**
     * @brief This method returns the information about an article in XML and JSON
     *        formats.
     * @param id identifier of the article as query param.
     * @return an HTTP response (OK if the article is found, not found if the
     *         article does not exist in the database)
     */
    @GetMapping(value = "/showSkindiseases/{id}", produces = { "application/json", "application/xml" })
    public ResponseEntity<Skindiseases>getskindiseaseID(@PathVariable int id) {

        Optional<Skindiseases> skindisease = skin_repository.findById(id);

        if (skindisease.isPresent()) {
            return new ResponseEntity<>(skindisease.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping(value = "/addSkindiseases", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Skindiseases> addskindisease(@RequestBody Skindiseases skindisease) {

       
            skin_repository.save(skindisease);
            return new ResponseEntity<>(skindisease, HttpStatus.CREATED);
       

    }

    @PutMapping(value = "/modifySkindiseases/{id}", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Skindiseases> putskindisease(@PathVariable int id, @RequestBody Skindiseases disease) {

        Optional<Skindiseases> found_disease = skin_repository.findById(id);

        if (found_disease.isPresent()) {

            found_disease.get().setDisease(disease.getDisease());
            found_disease.get().setICD_code(disease.getICD_code());
            found_disease.get().setStandard_treatment(disease.getStandard_treatment());
            found_disease.get().setMedications(disease.getMedications());
            found_disease.get().setAlternatives(disease.getAlternatives());
            found_disease.get().setRecommendations(disease.getRecommendations());
            found_disease.get().setReferral(disease.getReferral());
            found_disease.get().setSource(disease.getSource());
            skin_repository.save(found_disease.get());
            return new ResponseEntity<>(disease, HttpStatus.OK);

        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @DeleteMapping(value = "/deleteSkindisease") 
    public ResponseEntity<Skindiseases> deleteskindisease(@RequestParam int id) {

        Optional<Skindiseases> found_skindisease = skin_repository.findById(id);

        if (found_skindisease.isPresent()) {

            skin_repository.delete(found_skindisease.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
