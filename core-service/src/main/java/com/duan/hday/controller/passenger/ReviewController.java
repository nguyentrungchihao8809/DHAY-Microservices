package com.duan.hday.controller.passenger;

import com.duan.hday.entity.User;
import com.duan.hday.entity.enums.ReviewType;
import com.duan.hday.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.duan.hday.config.UserPrincipal;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitReview(
            @AuthenticationPrincipal UserPrincipal principal, // Inject UserPrincipal đã login
            @RequestParam Long tripId,
            @RequestParam Long revieweeId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @RequestParam ReviewType type
    ) {
        // Lấy đối tượng User thực thể từ Principal
        User reviewer = principal.getUser(); 

        // Kiểm tra không cho tự đánh giá chính mình
        if (reviewer.getId().equals(revieweeId)) {
            return ResponseEntity.badRequest().body("Bạn không thể tự đánh giá chính mình!");
        }

        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body("Rating phải từ 1 đến 5 sao");
        }

        reviewService.submitReview(tripId, reviewer, revieweeId, rating, comment, type);
        
        return ResponseEntity.ok("Cảm ơn bạn đã gửi đánh giá!");
    }
}
