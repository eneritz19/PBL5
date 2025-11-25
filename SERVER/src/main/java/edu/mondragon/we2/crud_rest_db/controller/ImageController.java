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

import edu.mondragon.we2.crud_rest_db.entity.Image;
import edu.mondragon.we2.crud_rest_db.repository.ImageRepository;
import edu.mondragon.we2.crud_rest_db.repository.UserRepository;
import jakarta.websocket.server.PathParam;

@RestController
@RequestMapping("/skinXpert")
public class ImageController {

    @Autowired
    ImageRepository image_repository;

    /**
     * @brief This method returns the list of articles in XML and JSON format.
     * @return an HTTP response (OK if there are articles in the database, not found
     *         if there are
     *         not articles)
     */

    
    @GetMapping(value = "/showImage", produces = { "application/json", "application/xml" })
    @ResponseBody
    public ResponseEntity<List<Image>>getImage() {

        List<Image> image_list = image_repository.findAll();

        if (image_list.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(image_list, HttpStatus.OK);
        }

    }
 
    /**
     * @brief This method returns the information about an article in XML and JSON
     *        formats.
     * @param id identifier of the article as query param.
     * @return an HTTP response (OK if the article is found, not found if the
     *         article does not exist in the database)
     */
    @GetMapping(value = "/showImage/{id}", produces = { "application/json", "application/xml" })
    public ResponseEntity<Image>getImageID(@PathVariable int id) {

        Optional<Image> image = image_repository.findById(id);

        if (image.isPresent()) {
            return new ResponseEntity<>(image.get(), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping(value = "/addImage", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Image> addImage(@RequestBody Image image) {

       
            image_repository.save(image);
            return new ResponseEntity<>(image, HttpStatus.CREATED);
       

    }

    @PutMapping(value = "/modifyImage/{id}", consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Image> putImage(@PathVariable int id, @RequestBody Image image) {

        Optional<Image> found_image = image_repository.findById(id);

        if (found_image.isPresent()) {

            found_image.get().setFile_path(image.getFile_path());
            found_image.get().setUpload_date(image.getUpload_date());
            image_repository.save(found_image.get());
            return new ResponseEntity<>(image, HttpStatus.OK);

        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @DeleteMapping(value = "/deleteImage") 
    public ResponseEntity<Image> deleteImage(@RequestParam int id) {

        Optional<Image> found_image = image_repository.findById(id);

        if (found_image.isPresent()) {

            image_repository.delete(found_image.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
