package com.web.lab1.service;

import com.web.lab1.data.*;
import com.web.lab1.repository.CompetitionRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompetitionService {

    public Optional<Competition> createCompetition(CompetitionForm form) {
        var userDetails = (DefaultOidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getPreferredUsername();

        if (form.getName().isEmpty() ||
            username.isEmpty() ||
            form.getCompetitors().isEmpty() ||
            form.getWin().isEmpty() ||
            form.getDraw().isEmpty() ||
            form.getLoss().isEmpty()) {
            return Optional.empty();
        }
        try {
            var win = Double.parseDouble(form.getWin());
            var draw = Double.parseDouble(form.getDraw());
            var loss = Double.parseDouble(form.getLoss());
            Score scoring = new Score(win, draw, loss);

            List<String> competitorsList = Arrays.stream(form.getCompetitors().split(";"))
                    .map(String::strip)
                    .toList();
            Set<String> set = new HashSet<>(competitorsList);
            if(set.size() < competitorsList.size()) return Optional.empty();
            if (competitorsList.size() < 4 || competitorsList.size() > 8) return Optional.empty();

            var competition = new Competition(form.getName(), username, competitorsList, scoring);
            var repository = new CompetitionRepository();
            repository.saveCompetition(competition);
            return Optional.of(competition);
        }
        catch(Exception e) {
            return Optional.empty();
        }
    }

    public Optional<CompetitionPageForm> getCompetition(String competitionId) {
        var repository = new CompetitionRepository();
        var competition = repository.getCompetitionById(competitionId);
        if (competition.isPresent()) {
            var competitionObj = competition.get();
            double win = competitionObj.getScoring().getWin();
            double draw = competitionObj.getScoring().getDraw();
            double loss = competitionObj.getScoring().getLoss();
            return Optional.of(new CompetitionPageForm(competitionObj.getName(),
                    competitionObj.getUsername(),
                    competitionObj.getCompetitors().toArray(new String[0]),
                    win % 1 == 0 ? String.valueOf((int)win) : String.valueOf(win),
                    draw % 1 == 0 ? String.valueOf((int)draw) : String.valueOf(draw),
                    loss % 1 == 0 ? String.valueOf((int)loss) : String.valueOf(loss)));
        }
        return Optional.empty();
    }

    public Optional<Schedule> getScheduleData(int numOfCompetitors) {
        var repository = new CompetitionRepository();
        return repository.getScheduleByCompetitorNumber(numOfCompetitors);
    }

    public Optional<Schedule> getScheduleScores(String scheduleId) {
        var repository = new CompetitionRepository();
        return repository.getScheduleScoresById(scheduleId);
    }

    public Optional<Schedule> updateSchedule(Schedule schedule, String competitionId) {
        var repository = new CompetitionRepository();
        Map<String, Map<Integer, String>> data = new HashMap<>();
        Map<Integer, String> fullScores = new HashMap<>();
        for (int index = 0; index < schedule.getSchedules().size(); index++) {
            var round = schedule.getSchedules().get(index);

            List<String> tempList = new ArrayList<>();
            for (List<String> match : round) {
                if (match.contains(null) || match.contains("")) {
                    List<String> tmp = new ArrayList<>();
                    tmp.add("-1");
                    tmp.add("-1");
                    tempList.add(tmp.stream().map(String::valueOf).collect(Collectors.joining(",")));
                } else {
                    tempList.add(match.stream().map(String::valueOf).collect(Collectors.joining(",")));
                }
            }
            String newScore = tempList.stream().map(String::valueOf).collect(Collectors.joining("*"));
            newScore = newScore.replace("null", "-1");
            fullScores.put(index, newScore);
        }
        data.put(competitionId, fullScores);
        repository.updateScheduleById(data);
        return Optional.empty();
    }
}
