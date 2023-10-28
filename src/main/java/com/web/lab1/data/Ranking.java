package com.web.lab1.data;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Ranking {

    private List<Map.Entry<String, String>> rankings;

    public Ranking(List<Map.Entry<String, String>> rankings) {
        this.rankings = rankings;
    }
    public Ranking() {
        this.rankings = new ArrayList<>();
    }
}
