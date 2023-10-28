package com.web.lab1.data;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CompetitionCardData {

    private List<CompetitionCard> allCompetitions;

    public CompetitionCardData(List<CompetitionCard> allCompetitions) {
        this.allCompetitions = allCompetitions;
    }
}
