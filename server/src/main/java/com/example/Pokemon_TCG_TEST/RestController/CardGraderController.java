package com.example.Pokemon_TCG_TEST.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.Pokemon_TCG_TEST.Service.CardGraderService;

@RestController
@RequestMapping("/api/grader")
@CrossOrigin(origins = "http://localhost:4200")
public class CardGraderController {

    private final CardGraderService graderService;

    public CardGraderController(CardGraderService graderService) {
        this.graderService = graderService;
    }

    @PostMapping("/grade")
    public ResponseEntity<?> gradeCard(@RequestParam("frontImage") MultipartFile frontImage,
                                       @RequestParam("backImage") MultipartFile backImage) {
        return graderService.gradeCard(frontImage, backImage);
    }
    
}