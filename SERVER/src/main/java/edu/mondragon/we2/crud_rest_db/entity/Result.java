package edu.mondragon.we2.crud_rest_db.entity;

import lombok.Data;

import java.security.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // This line indicates that the id is an auto_increment value
    private int id_result;
    private int id_request;
    private String diagnosis;
    private float confidence_level;
    private String recommendations;
    private Timestamp analysis_date;
    private int id_doctor;

    public Result() {
    }

    public Result(int id_result, int id_request, String diagnosis, float confidence_level, String recommendations,
            Timestamp analysis_date, int id_doctor) {
        this.id_result = id_result;
        this.id_request = id_request;
        this.diagnosis = diagnosis;
        this.confidence_level = confidence_level;
        this.recommendations = recommendations;
        this.analysis_date = analysis_date;
        this.id_doctor = id_doctor;
    }
 
    

    public int getId_result() {
        return this.id_result;
    }

    public void setId_result(int id_result) {
        this.id_result = id_result;
    }

    public int getId_request() {
        return this.id_request;
    }

    public void setId_request(int id_request) {
        this.id_request = id_request;
    }

    public String getDiagnosis() {
        return this.diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public float getConfidence_level() {
        return this.confidence_level;
    }

    public void setConfidence_level(float confidence_level) {
        this.confidence_level = confidence_level;
    }

    public String getRecommendations() {
        return this.recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public Timestamp getAnalysis_date() {
        return this.analysis_date;
    }

    public void setAnalysis_date(Timestamp analysis_date) {
        this.analysis_date = analysis_date;
    }

    public int getId_doctor() {
        return this.id_doctor;
    }

    public void setId_doctor(int id_doctor) {
        this.id_doctor = id_doctor;
    }
    
}

