package com.bear.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author LiuShanshan
 * @version V1.0
 * @Description
 */
@RestController
public class TestController {


    @GetMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam("test") String test){
        return test;
    }

    @GetMapping("/hello2")
    @ResponseBody
    public String hello(){
        return "test";
    }

}
