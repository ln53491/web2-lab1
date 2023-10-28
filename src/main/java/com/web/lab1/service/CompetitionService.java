package com.web.lab1.service;

import com.web.lab1.data.*;
import com.web.lab1.repository.CompetitionRepository;
import org.checkerframework.checker.units.qual.K;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CompetitionService {

    public Optional<Competition> createCompetition(CompetitionForm form) {
        var userDetails = (DefaultOidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getPreferredUsername();

        System.out.println(form);

        if (form.getName().isEmpty() ||
            username.isEmpty() ||
            form.getCompetitors().isEmpty() ||
            form.getWin().isEmpty() ||
            form.getDraw().isEmpty() ||
            form.getLoss().isEmpty()) {
            return Optional.empty();
        }
        try {
            System.out.println(form);
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
            String key = repository.saveCompetition(competition);

            Map<String, String> scoresToSave = new TreeMap<>();
            switch (competitorsList.size()) {
                case 4:
                    for (int i = 0; i < 3; i++) {
                        scoresToSave.put(String.valueOf(i), "-1,-1*-1,-1");
                    }
                    break;
                case 5:
                    for (int i = 0; i < 5; i++) {
                        scoresToSave.put(String.valueOf(i), "-1,-1*-1,-1");
                    }
                    break;
                case 6:
                    for (int i = 0; i < 5; i++) {
                        scoresToSave.put(String.valueOf(i), "-1,-1*-1,-1*-1,-1");
                    }
                    break;
                case 7:
                    for (int i = 0; i < 7; i++) {
                        scoresToSave.put(String.valueOf(i), "-1,-1*-1,-1*-1,-1");
                    }
                    break;
                case 8:
                    for (int i = 0; i < 7; i++) {
                        scoresToSave.put(String.valueOf(i), "-1,-1*-1,-1*-1,-1*-1,-1");
                    }
                default:
                    break;
            }
            System.out.println(scoresToSave);
            repository.saveScores(key, scoresToSave);
            return Optional.of(competition);
        }
        catch(Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void deleteCompetition(String competitionId) {
        var repository = new CompetitionRepository();
        repository.deleteCompetitionById(competitionId);
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

    public Optional<CompetitionCardData> getCompetitionCardDataByUser() {
        var userDetails = (DefaultOidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getPreferredUsername();

        var repository = new CompetitionRepository();
        List<CompetitionCard> allCompetitions = repository.getCompetitionCardDataByUser(username);

        return Optional.of(new CompetitionCardData(allCompetitions));
    }

    public Optional<Schedule> getScheduleData(int numOfCompetitors) {
        var repository = new CompetitionRepository();
        return repository.getScheduleByCompetitorNumber(numOfCompetitors);
    }

    public Optional<Schedule> getScheduleScores(String scheduleId) {
        var repository = new CompetitionRepository();
        return repository.getScheduleScoresById(scheduleId);
    }

    public Optional<Ranking> getRankings(String scheduleId) {
        var repository = new CompetitionRepository();
        var scores = repository.getScheduleScoresById(scheduleId);
        var competitionInfo = repository.getCompetitionById(scheduleId);

        if (scores.isPresent() && competitionInfo.isPresent()) {
            var scoringSystem = competitionInfo.get().getScoring();
            var competitors = competitionInfo.get().getCompetitors();
            var schedules = getScheduleData(competitors.size()).get().getSchedules();
            var realScores = scores.get().getSchedules();

            Map<String, Double> rankings = new TreeMap<>();
            for (String competitor : competitors) {
                rankings.put(competitor, 0.0);
            }

            for (int i = 0; i < realScores.size(); i++) {
                List<List<String>> currRound = realScores.get(i);
                for (int j = 0; j < currRound.size(); j++) {
                    List<String> currMatch = currRound.get(j);
                    if (!currMatch.contains(null) && !currMatch.contains("")) {
                        String firstOpponent = competitors.get(Integer.parseInt(schedules.get(i).get(j).get(0)) - 1);
                        String secondOpponent = competitors.get(Integer.parseInt(schedules.get(i).get(j).get(1)) - 1);

                        if (Objects.equals(currMatch.get(0), currMatch.get(1))) {
                            rankings.put(firstOpponent, rankings.get(firstOpponent) + scoringSystem.getDraw());
                            rankings.put(secondOpponent, rankings.get(secondOpponent) + scoringSystem.getDraw());
                        } else if(Integer.parseInt(currMatch.get(0)) > Integer.parseInt(currMatch.get(1))) {
                            rankings.put(firstOpponent, rankings.get(firstOpponent) + scoringSystem.getWin());
                            rankings.put(secondOpponent, rankings.get(secondOpponent) + scoringSystem.getLoss());
                        } else if(Integer.parseInt(currMatch.get(0)) < Integer.parseInt(currMatch.get(1))) {
                            rankings.put(firstOpponent, rankings.get(firstOpponent) + scoringSystem.getLoss());
                            rankings.put(secondOpponent, rankings.get(secondOpponent) + scoringSystem.getWin());
                        }
                    }
                }
            }

            TreeMap<String, String> rankingsMap = new TreeMap<>();
            for (Map.Entry<String, Double> entry : rankings.entrySet()) {
                rankingsMap.put(entry.getKey(), entry.getValue() % 1 == 0 ? String.valueOf(entry.getValue().intValue()) : String.valueOf(entry.getValue()));
            }

            List<Map.Entry<String, String>> sorted =
                    new ArrayList<>(rankingsMap.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue()).toList());
            Collections.reverse(sorted);
            return Optional.of(new Ranking(sorted));
        }
        return Optional.empty();
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
