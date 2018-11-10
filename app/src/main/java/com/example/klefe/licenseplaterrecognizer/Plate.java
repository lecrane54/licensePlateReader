package com.example.klefe.licenseplaterrecognizer;

/**
 * Created by klefe on 11/8/18.
 */

public class Plate {
    String plate;
    String plateId;
    String user;
    String state;

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getPlateId() {
        return plateId;
    }

    public void setPlateId(String plateId) {
        this.plateId = plateId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Plate(){}

    public Plate(String plate, String plateId, String user, String state){
        this.plate = plate;
        this.plateId = plateId;
        this.user = user;
        this.state = state;

    }
}
