package edu.mondragon.we2.crud_rest_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mondragon.we2.crud_rest_db.entity.Result;

public interface ResultRepository extends JpaRepository<Result, Integer> {

}