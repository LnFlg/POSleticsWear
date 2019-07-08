package com.example.posleticswear;

import android.location.Location;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Map;

public class RuntimeData {

    public static final int LOCATION_PERMISSION_CODE = 1;

    private static RuntimeData instance;
    private int userId = 0;
    private ArrayList<Pos> route = null;
    private Map<Integer, Pos> allPos;
    private double discoveryRadius=500.0;
    private boolean disableLocationServices = false;

    private RuntimeData() {
        super();
    }

    public static synchronized RuntimeData getInstance() {
        if (instance == null) {
            instance = new RuntimeData();
        }
        return instance;
    }

    public void addToRoute(int id){
        this.route.add(new Pos(allPos.get(Integer.valueOf(id)).getLoc(),id));
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public ArrayList<Pos> getRoute() {
        return route;
    }
    public void setRoute(ArrayList<Pos> route) {
        this.route = route;
    }
    public Map<Integer,Pos> getAllPos() {
        return allPos;
    }
    public void setAllPos(Map<Integer, Pos> allPos) {
        this.allPos = allPos;
    }
    public double getDiscoveryRadius() {
        return discoveryRadius;
    }

    public void setDiscoveryRadius(double discoveryRadius) {
        this.discoveryRadius = discoveryRadius;
    }

    public boolean isDisableLocationServices() {
        return disableLocationServices;
    }

    public void setDisableLocationServices(boolean disableLocationServices) {
        this.disableLocationServices = disableLocationServices;
    }
}
