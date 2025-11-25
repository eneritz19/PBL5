package edu.mondragon.we2.crud_rest_db.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
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
    private float confidence_level;
    private int id_skindiseases;
    private int id_doctor;


    @Column(name = "analysis_date", insertable = false, updatable = false)
    private LocalDateTime analysis_date;


    public Result() {
    }

    public Result(int id_result, int id_request, float confidence_level, int id_skindiseases, LocalDateTime analysis_date,
            int id_doctor) {
        this.id_result = id_result;
        this.id_request = id_request;
        this.confidence_level = confidence_level;
        this.id_skindiseases = id_skindiseases;
        this.analysis_date = analysis_date;
        this.id_doctor = id_doctor;
    }

    public int getId_result() {
        return id_result;
    }

    public void setId_result(int id_result) {
        this.id_result = id_result;
    }

    public int getId_request() {
        return id_request;
    }

    public void setId_request(int id_request) {
        this.id_request = id_request;
    }

    public float getConfidence_level() {
        return confidence_level;
    }

    public void setConfidence_level(float confidence_level) {
        this.confidence_level = confidence_level;
    }

    public int getId_skindiseases() {
        return id_skindiseases;
    }

    public void setId_skindiseases(int id_skindiseases) {
        this.id_skindiseases = id_skindiseases;
    }

    public LocalDateTime getAnalysis_date() {
        return analysis_date;
    }

    public void setAnalysis_date(LocalDateTime analysis_date) {
        this.analysis_date = analysis_date;
    }

    public int getId_doctor() {
        return id_doctor;
    }

    public void setId_doctor(int id_doctor) {
        this.id_doctor = id_doctor;
    }
}

