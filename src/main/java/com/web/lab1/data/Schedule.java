package com.web.lab1.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Schedule {

    private List<List<List<String>>> schedules;

    public Schedule(List<List<List<String>>> schedules) {
        this.schedules = schedules;
    }
    public Schedule() {
        this.schedules = new ArrayList<>();
    }
}