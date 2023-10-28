package com.web.lab1.controller;

import com.web.lab1.data.CompetitionForm;
import com.web.lab1.data.Schedule;
import com.web.lab1.service.CompetitionService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Objects;

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
        var competitionForm = this.competitionService.getCompetition(competitionId);
        if (competitionForm.isPresent()) {
            var form = competitionForm.get();
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication.getPrincipal() == "anonymousUser") {
                model.addAttribute("allowed", false);
            } else {
                var userDetails = (DefaultOidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String username = userDetails.getPreferredUsername();
                if (Objects.equals(username, form.getUsername())) {
                    model.addAttribute("allowed", true);
                } else {
                    model.addAttribute("allowed", false);
                }
            }
            var schedule = this.competitionService.getScheduleData(form.getCompetitors().length);
            var scores = this.competitionService.getScheduleScores(competitionId);
            var ranking = this.competitionService.getRankings(competitionId);
            if (schedule.isPresent() && scores.isPresent() && ranking.isPresent()) {
                model.addAttribute("scores", scores.get());
                model.addAttribute("ranking", ranking.get());
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

    @GetMapping("/competition/delete/{id}")
    @PreAuthorize("hasAuthority('SCOPE_profile')")
    public String deleteCompetition(@PathVariable("id") String competitionId) {
        this.competitionService.deleteCompetition(competitionId);
        return "redirect:/";
    }
}
