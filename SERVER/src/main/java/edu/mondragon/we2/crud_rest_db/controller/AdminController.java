package edu.mondragon.we2.crud_rest_db.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.mondragon.we2.crud_rest_db.entity.Admin;
import edu.mondragon.we2.crud_rest_db.repository.AdminRepository;

@RestController
@RequestMapping("/skinXpert")
public class AdminController {

    @Autowired
    AdminRepository admin_repository;

    /**
     * @brief This method returns the list of articles in XML and JSON format.
     * @return an HTTP response (OK if there are articles in the database, not found
     *         if there are
     *         not articles)
     */

    
    @GetMapping(value = "/showAdmin", produces = { "application/json", "application/xml" })
    @ResponseBody
    public ResponseEntity<List<Admin>>getAdmin() {

        List<Admin> admin_list = admin_repository.findAll();

        if (admin_list.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(admin_list, HttpStatus.OK);
        }

    }
}
