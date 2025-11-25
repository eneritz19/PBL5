package edu.mondragon.we2.crud_rest_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mondragon.we2.crud_rest_db.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Integer> {

}