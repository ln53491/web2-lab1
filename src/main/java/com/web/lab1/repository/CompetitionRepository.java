package com.web.lab1.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.*;
import com.web.lab1.data.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class CompetitionRepository {
    private String competitionsPathName = "competitions";
    private String schedulePathName = "schedule";
    private String scoresPathName = "scores";
    private DatabaseReference competitionsTable;
    private DatabaseReference scheduleTable;
    private DatabaseReference scoresTable;

    public CompetitionRepository() {
        this.competitionsTable = FirebaseDatabase.getInstance().getReference(this.competitionsPathName);
        this.scheduleTable = FirebaseDatabase.getInstance().getReference(this.schedulePathName);
        this.scoresTable = FirebaseDatabase.getInstance().getReference(this.scoresPathName);
    }

    public String saveCompetition(Competition competition) {
        DatabaseReference child = this.competitionsTable.push();
        child.setValue(competition, (err, ref) -> {});
        return child.getKey();
    }

    public void saveScores(String key, Map<String, String> scores) {
        for (Map.Entry<String, String> entry : scores.entrySet()) {
            this.scoresTable.child(key).child(entry.getKey()).setValueAsync(entry.getValue());
        }
    }

    public void deleteCompetitionById(String competitionId) {
        this.competitionsTable.child(competitionId).removeValueAsync();
        this.scoresTable.child(competitionId).removeValueAsync();
    }

    public List<CompetitionCard> getCompetitionCardDataByUser(String username) {
        final List<CompetitionCard> cards = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        this.competitionsTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    String name = childSnapshot.child("name").getValue(String.class);
                    String newUsername = childSnapshot.child("username").getValue(String.class);

                    if (Objects.equals(newUsername, username)) {
                        var compCard = new CompetitionCard(key, name);
                        cards.add(compCard);
                    }
                }
                latch.countDown();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Listener canceled: " + databaseError.getMessage());
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return cards;
    }

    public Optional<Schedule> getScheduleByCompetitorNumber(int numOfCompetitors) {
        final CountDownLatch latch = new CountDownLatch(1);
        final Schedule[] schedules = new Schedule[1];
        this.scheduleTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                var scheduleId = String.valueOf(numOfCompetitors);
                if (dataSnapshot.child(scheduleId).exists()) {
                    List<String> scheduleObj = (List<String>)dataSnapshot.child(scheduleId).getValue();
                    List<List<List<String>>> schedulesToAdd = new ArrayList<>();
                    for (String round : scheduleObj) {
                        schedulesToAdd.add(Arrays.stream(round.split("-"))
                                .map(str -> Arrays.stream(str.split(",")).toList())
                                .toList());
                    }
                    schedules[0] = new Schedule(schedulesToAdd);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        if (schedules[0] != null) {
            return Optional.of(schedules[0]);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Competition> getCompetitionById(String competitionId) {
        final CountDownLatch latch = new CountDownLatch(1);
        final Competition[] competition = new Competition[1];
        this.competitionsTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(competitionId).exists()) {
                    var competitionObj = dataSnapshot.child(competitionId);
                    String name = competitionObj.child("name").getValue(String.class);
                    String username = competitionObj.child("username").getValue(String.class);
                    var scoringObj = competitionObj.child("scoring");
                    Score scoring = new Score(scoringObj.child("win").getValue(Double.class),
                                                scoringObj.child("draw").getValue(Double.class),
                                                scoringObj.child("loss").getValue(Double.class));
                    List<String> competitors = (List<String>)competitionObj.child("competitors").getValue();

                    competition[0] = new Competition(name, username, competitors, scoring);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (competition[0] != null) {
            return Optional.of(competition[0]);
        } else {
            return Optional.empty();
        }
    }

    public void updateScheduleById(Map<String, Map<Integer, String>> data) {
        for (Map.Entry<String, Map<Integer, String>> entry : data.entrySet()) {
            DatabaseReference childReference = this.scoresTable.child(entry.getKey());

            for (Map.Entry<Integer, String> innerEntry : entry.getValue().entrySet()) {
                childReference.child(String.valueOf(innerEntry.getKey())).setValueAsync(innerEntry.getValue());
            }
        }
    }

    public Optional<Schedule> getScheduleScoresById(String scheduleId) {
        final CountDownLatch latch = new CountDownLatch(1);
        final Schedule[] schedules = new Schedule[1];
        this.scoresTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(scheduleId).exists()) {
                    List<String> scheduleObj = (List<String>)dataSnapshot.child(scheduleId).getValue();
                    List<List<List<String>>> schedulesToAdd = new ArrayList<>();
                    for (String round : scheduleObj) {
                        schedulesToAdd.add(Arrays.stream(round.split("\\*"))
                                .map(str -> Arrays.stream(str.split(",")).map(x -> (Objects.equals(x, "-1") ? "" : x)).toList())
                                .toList());
                    }
                    schedules[0] = new Schedule(schedulesToAdd);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        if (schedules[0] != null) {
            return Optional.of(schedules[0]);
        } else {
            return Optional.empty();
        }
    }
}
