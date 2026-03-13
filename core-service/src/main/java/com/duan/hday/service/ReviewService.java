package com.duan.hday.service;

import com.duan.hday.entity.*;
import com.duan.hday.entity.enums.ReviewType;
import com.duan.hday.entity.enums.TripStatus;
import com.duan.hday.repository.passenger.ReviewRepository;
import com.duan.hday.repository.auth.UserRepository;
import com.duan.hday.repository.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional
    public void submitReview(Long tripId, User reviewer, Long revieweeId, 
                            Integer rating, String comment, ReviewType type) {
        
        // 1. Tìm Trip & Kiểm tra trạng thái
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new RuntimeException("Chuyến đi chưa kết thúc, chưa thể đánh giá!");
        }

        // 2. Kiểm tra xem đã đánh giá chưa (Tránh spam)
        if (reviewRepository.existsByTripIdAndReviewerIdAndRevieweeId(tripId, reviewer.getId(), revieweeId)) {
            throw new RuntimeException("Bạn đã gửi đánh giá cho chuyến đi này rồi!");
        }

        // 3. Tìm người được đánh giá (Xử lý an toàn)
        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new RuntimeException("Người được đánh giá không tồn tại"));

        // 4. Lưu Review trước
        Review review = Review.builder()
                .trip(trip)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(rating)
                .comment(comment)
                .type(type)
                .build();
        reviewRepository.save(review);

        // 5. Cập nhật điểm cho User (Nên chạy sau khi lưu review thành công)
        updateUserRating(reviewee, rating);
    }

    private void updateUserRating(User reviewee, Integer newRating) {
        double currentAvg = (reviewee.getAverageRating() != null) ? reviewee.getAverageRating() : 5.0;
        int total = (reviewee.getTotalReviews() != null) ? reviewee.getTotalReviews() : 0;
        
        double newAvg = ((currentAvg * total) + newRating) / (total + 1);
        
        reviewee.setAverageRating(Math.round(newAvg * 10.0) / 10.0);
        reviewee.setTotalReviews(total + 1);
        userRepository.save(reviewee);
    }
}