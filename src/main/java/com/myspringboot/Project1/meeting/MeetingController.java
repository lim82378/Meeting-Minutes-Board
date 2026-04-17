package com.myspringboot.Project1.meeting;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    
    // 목록 화면 이동
    @GetMapping("/list")
    public String list() {
        return "list"; 
    }

    // 등록 화면 이동
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    // 상세 조회 화면 이동
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable(value = "id") Long id, Model model) {
        model.addAttribute("meetingId", id); 
        return "detail";
    }
    // 수정 화면 이동 
    @GetMapping("/update/{id}")
    public String updatePage(@PathVariable(value = "id") Long id, Model model) {
        model.addAttribute("meetingId", id); 
        return "update"; 
    }
}