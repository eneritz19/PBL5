package edu.mondragon.we2.crud_rest_db.entity;

import lombok.Data;

import java.security.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // This line indicates that the id is an auto_increment value
    private int id_image;
    private int id_request;
    private String file_path;
    private Timestamp upload_date;

    public Image() {
    }

    public Image(int id_image, int id_request, String file_path, Timestamp upload_date) {
        this.id_image = id_image;
        this.id_request = id_request;
        this.file_path = file_path;
        this.upload_date = upload_date;
    }
    

    public int getId_image() {
        return this.id_image;
    }

    public void setId_image(int id_image) {
        this.id_image = id_image;
    }

    public int getId_request() {
        return this.id_request;
    }

    public void setId_request(int id_request) {
        this.id_request = id_request;
    }

    public String getFile_path() {
        return this.file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public Timestamp getUpload_date() {
        return this.upload_date;
    }

    public void setUpload_date(Timestamp upload_date) {
        this.upload_date = upload_date;
    }
}

