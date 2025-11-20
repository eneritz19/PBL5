package edu.mondragon.we2.rest_client_Example;

import java.io.Serializable;

import lombok.Data;

@Data
public class City {

    private int idCity;
    private String cityName;
    private int maxTemperature;
    private int minTemperature;
    private String weather;
    private int windSpeed;
    private double rain;

    public City() {
    }

    public City(int idCity, String cityName, int maxTemperature,
            int minTemperature, String weather, int windSpeed, double rain) {

        this.idCity = idCity;
        this.cityName = cityName;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.weather = weather;
        this.windSpeed = windSpeed;
        this.rain = rain;

    }

    public int getIdCity() {
        return idCity;
    }

    public void setIdCity(int idCity) {
        this.idCity = idCity;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(int minTemperature) {
        this.minTemperature = minTemperature;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getRain() {
        return rain;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }

}
