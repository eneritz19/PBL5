package edu.mondragon.we2.crud_rest_db.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.mondragon.we2.crud_rest_db.entity.Doctor;
import edu.mondragon.we2.crud_rest_db.repository.DoctorRepository;

@RestController
@RequestMapping("/skinXpert")
public class DoctorsController {

    @Autowired
    DoctorRepository doctor_repository;

    /**
     * @brief This method returns the list of articles in XML and JSON format.
     * @return an HTTP response (OK if there are articles in the database, not found
     *         if there are
     *         not articles)
     */

    
    @GetMapping(value = "/showDoctors", produces = { "application/json", "application/xml" })
    @ResponseBody
    public ResponseEntity<List<Doctor>>getDoctors() {

        List<Doctor> doctor_list = doctor_repository.findAll();

        if (doctor_list.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(doctor_list, HttpStatus.OK);
        }

    }
 
    /**
     * @brief This method returns the information about an article in XML and JSON
     *        formats.
     * @param id identifier of the article as query param.
     * @return an HTTP response (OK if the article is found, not found if the
     *         article does not exist in the database)
     */
    @GetMapping(value = "/showDoctor/{id}", produces = { "application/json", "application/xml" })
    public ResponseEntity<Doctor>getDoctorID(@PathVariable int id) {

        Optional<Doctor> doctor = doctor_repository.findById(id);

        if (doctor.isPresent()) {
            return new ResponseEntity<>(doctor.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping(value = "/doctorName", produces = { "application/json", "application/xml" })
    public ResponseEntity<Doctor> getDoctorByName(@RequestParam String name) {

        Doctor doctor = doctor_repository.findByName(name);

        if (doctor == null) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(doctor, HttpStatus.OK);
        }
    }

}
