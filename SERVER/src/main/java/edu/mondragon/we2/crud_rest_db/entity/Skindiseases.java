package edu.mondragon.we2.crud_rest_db.entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "skin_diseases")
public class Skindiseases {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // This line indicates that the id is an auto_increment value
    private int id_skindiseases;
    private String disease;
    private String ICD_code;
    private String standard_treatment;
    private String medications;
    private String alternatives;
    private String recommendations;
    private String referral;
    private String source;

    public Skindiseases() {
    }



    public Skindiseases(int id_skindiseases, String disease, String ICD_code, String standard_treatment, String medications, String alternatives, String recommendations, String referral, String source) {
        this.id_skindiseases = id_skindiseases;
        this.disease = disease;
        this.ICD_code = ICD_code;
        this.standard_treatment = standard_treatment;
        this.medications = medications;
        this.alternatives = alternatives;
        this.recommendations = recommendations;
        this.referral = referral;
        this.source = source;
    }

    public int getId_skindiseases() {
        return this.id_skindiseases;
    }

    public void setId_skindiseases(int id_skindiseases) {
        this.id_skindiseases = id_skindiseases;
    }

    public String getDisease() {
        return this.disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getICD_code() {
        return this.ICD_code;
    }

    public void setICD_code(String ICD_code) {
        this.ICD_code = ICD_code;
    }

    public String getStandard_treatment() {
        return this.standard_treatment;
    }

    public void setStandard_treatment(String standard_treatment) {
        this.standard_treatment = standard_treatment;
    }

    public String getMedications() {
        return this.medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getAlternatives() {
        return this.alternatives;
    }

    public void setAlternatives(String alternatives) {
        this.alternatives = alternatives;
    }

    public String getRecommendations() {
        return this.recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getReferral() {
        return this.referral;
    }

    public void setReferral(String referral) {
        this.referral = referral;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }


}