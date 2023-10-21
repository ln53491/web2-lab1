package com.web.lab1.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${okta.oauth2.issuer}")
    private String issuer;
    @Value("${okta.oauth2.client-id}")
    private String clientId;

    @GetMapping("/")
    String home() {
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

//    @GetMapping("/profile")
//    @PreAuthorize("hasAuthority('SCOPE_profile')")
//    ModelAndView userDetails(OAuth2AuthenticationToken authentication) {
//        return new ModelAndView("profile", Collections.singletonMap("details", authentication.getPrincipal().getAttributes()));
//    }
}