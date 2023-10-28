package com.web.lab1.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompetitionCard {

    private String id;
    private String name;

    public CompetitionCard(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
