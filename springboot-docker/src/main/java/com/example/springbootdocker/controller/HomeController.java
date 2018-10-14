package com.example.springbootdocker.controller;

import com.example.springbootdocker.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/home")
    public String home(@RequestParam("filename") String filename) throws Exception {
        logger.info("comes to home() function. start");
        FileUtil.createFile(filename);
        logger.info("comes to home() function. end");
        return "home";
    }

}
