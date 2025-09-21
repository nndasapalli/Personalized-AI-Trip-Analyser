package com.easemytrip.controller;

import com.easemytrip.model.TripRequest;
import com.easemytrip.dto.PlannerResponse;
import com.easemytrip.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "https://63ee58dcb405.ngrok-free.app"})
public class TripPlannerController {


    private final AIService aiService;

    @PostMapping("/plan")
    public PlannerResponse planTrip(@RequestBody TripRequest request) {
        return aiService.generatePlannerResponse(request);
    }
}

