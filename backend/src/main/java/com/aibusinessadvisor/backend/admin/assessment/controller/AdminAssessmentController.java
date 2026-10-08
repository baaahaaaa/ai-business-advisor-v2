package com.aibusinessadvisor.backend.admin.assessment.controller;

import com.aibusinessadvisor.backend.admin.assessment.dto.AdminAssessmentResponse;
import com.aibusinessadvisor.backend.admin.assessment.service.AdminAssessmentService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/admin/assessments")
public class AdminAssessmentController {

    private final AdminAssessmentService
            adminAssessmentService;


    public AdminAssessmentController(
            AdminAssessmentService
                    adminAssessmentService
    ) {

        this.adminAssessmentService =
                adminAssessmentService;
    }


    @GetMapping
    public List<AdminAssessmentResponse>
            getAllAssessments() {

        return adminAssessmentService
                .getAllAssessments();
    }
}