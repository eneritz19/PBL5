package edu.mondragon.we2.crud_rest_db.entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // This line indicates that the id is an auto_increment value
    private int id_request;
    private int id_user;
    private Timestamp upload_date;
    private String status;
    private int assigned_doctor_id;

    public Request() {
    }

    public Request(int id_request, int id_user, Timestamp upload_date, String status, int assigned_doctor_id) {
        this.id_request = id_request;
        this.id_user = id_user;
        this.upload_date = upload_date;
        this.status = status;
        this.assigned_doctor_id = assigned_doctor_id;
    }


    public int getId_request() {
        return this.id_request;
    }

    public void setId_request(int id_request) {
        this.id_request = id_request;
    }

    public int getId_user() {
        return this.id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public Timestamp getUpload_date() {
        return this.upload_date;
    }

    public void setUpload_date(Timestamp upload_date) {
        this.upload_date = upload_date;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAssigned_doctor_id() {
        return this.assigned_doctor_id;
    }

    public void setAssigned_doctor_id(int assigned_doctor_id) {
        this.assigned_doctor_id = assigned_doctor_id;
    }

 
    
}

