package com.web.lab1.controller;

import com.web.lab1.data.CompetitionCardData;
import com.web.lab1.service.CompetitionService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;

@Controller
public class HomeController {

    @Value("${okta.oauth2.issuer}")
    private String issuer;
    @Value("${okta.oauth2.client-id}")
    private String clientId;

    @GetMapping("/")
    String home(Model model) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() == "anonymousUser") {
            model.addAttribute("competitionData", new CompetitionCardData(new ArrayList<>()));
        } else {
            var competitionService = new CompetitionService();
            var competitions = competitionService.getCompetitionCardDataByUser();
            model.addAttribute("competitionData", competitions.get());
        }
        return "home";
    }

    @GetMapping("/logout")
    public String customLogout(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            String endSessionEndpoint = "https://" + this.issuer + "/v1/logout?client_id=" + this.clientId + "&post_logout_redirect_uri=http://localhost:8080/";
            return "redirect:" + endSessionEndpoint;
        }
        return "redirect:/";
    }
}