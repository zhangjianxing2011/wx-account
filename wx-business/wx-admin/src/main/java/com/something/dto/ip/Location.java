package com.something.dto.ip;

import lombok.Data;

@Data
public class Location {
    private Double lat;
    private Double lng;
    public Location(Double lat,Double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
