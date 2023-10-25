package com.web.lab1.data;

import lombok.*;

@Data
@Builder
@Getter
@Setter
public class Score {
    private double win;
    private double draw;
    private double loss;

    public Score(double win, double draw, double loss) {
        this.win = win;
        this.draw = draw;
        this.loss = loss;
    }
}
