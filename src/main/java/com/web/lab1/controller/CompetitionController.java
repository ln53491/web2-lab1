package com.web.lab1.controller;

import com.web.lab1.data.CompetitionForm;
import com.web.lab1.data.Schedule;
import com.web.lab1.service.CompetitionService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@AllArgsConstructor
public class CompetitionController {

    private CompetitionService competitionService;

    @GetMapping("/competition")
    public String competition(Model model) {
        model.addAttribute("competitionForm", new CompetitionForm());
        return "competition";
    }

    @PostMapping("/competition")
    public String createCompetition(@ModelAttribute("competitionForm") CompetitionForm form) {
        var competition = this.competitionService.createCompetition(form);
        if (competition.isPresent()) {
            return "redirect:/";
        }
        return "redirect:/competition";
    }

    @GetMapping("/competition/{id}")
    public String getCompetition(@PathVariable("id") String competitionId, Model model) {
//        model.addAttribute("new_schedule", new Schedule());
        var competitionForm = this.competitionService.getCompetition(competitionId);
        if (competitionForm.isPresent()) {
            var form = competitionForm.get();
            var schedule = this.competitionService.getScheduleData(form.getCompetitors().length);
            var scores = this.competitionService.getScheduleScores(competitionId);
            if (schedule.isPresent() && scores.isPresent()) {
                model.addAttribute("scores", scores.get());
                System.out.println(scores.get().getSchedules());
                model.addAttribute("scheduleNames", schedule.get());
                model.addAttribute("competition", form);
                model.addAttribute("competitionId", competitionId);
                return "created_competition";
            }
        }
        return "redirect:/";
    }

    @PostMapping("/competition/{id}")
    @PreAuthorize("hasAuthority('SCOPE_profile')")
    public String setScores(@PathVariable("id") String competitionId, @ModelAttribute("schedule") Schedule schedule) {
        this.competitionService.updateSchedule(schedule, competitionId);
        return "redirect:/competition/" + competitionId;
    }
}
