package com.workzeno.workzeno.shared.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/v1/health")
public class HealthController {
    
    @GetMapping 
    public ApiHealthResponse health() {
        return new ApiHealthResponse("UP");
    }
    
}
