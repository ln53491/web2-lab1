package com.web.lab1.data;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class Competition{
    private String name;
    private String username;
    private List<String> competitors;
    private Score scoring;

    public Competition() {
    }

    public Competition(String name, String username, List<String> competitors, Score scoring) {
        this.name = name;
        this.username = username;
        this.competitors = competitors;
        this.scoring = scoring;
    }
}

