package edu.mondragon.we2.crud_rest_db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // This line indicates that the id is an auto_increment value
    private int id_doctor;
    private String name;
    private String email;
    private String role;
    private String created_at;

    public Doctor() {
    }

    public Doctor(int id_doctor, String name, String email, String role, String created_at) {
        this.id_doctor = id_doctor;
        this.name = name;
        this.email = email;
        this.role = role;
        this.created_at = created_at;
    }

    public int getId_doctor() {
        return id_doctor;
    }

    public void setId_doctor(int id_doctor) {
        this.id_doctor = id_doctor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
    
}

