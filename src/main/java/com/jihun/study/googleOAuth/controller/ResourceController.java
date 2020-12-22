package com.jihun.study.googleOAuth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping("/resource")
public class ResourceController {
    private String HOST_IP          = "";
    private String HOST_PORT        = "";
    private String HOST_PROTOCOL    = "http://";

    private String LOGO_IMG_PATH    = "";

    @Autowired
    ResourceController(Environment environment) throws UnknownHostException {
        HOST_IP         = InetAddress.getLocalHost().getHostAddress();
        HOST_PORT       = environment.getProperty("server.port", "8080");

        LOGO_IMG_PATH   = HOST_PROTOCOL + HOST_IP + ":" + HOST_PORT + "/images/google_logo.jpg";
    }
    
    @RequestMapping("/logo")
    public String getLogoImagePath() {
        return LOGO_IMG_PATH;
    }
}
