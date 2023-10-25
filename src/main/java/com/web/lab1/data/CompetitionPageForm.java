package com.web.lab1.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompetitionPageForm {

    private String name;
    private String username;
    private String[] competitors;
    private String win;
    private String draw;
    private String loss;

    public CompetitionPageForm(String name, String username, String[] competitors, String win, String draw, String loss) {
        this.name = name;
        this.username = username;
        this.competitors = competitors;
        this.win = win;
        this.draw = draw;
        this.loss = loss;
    }
}
