package com.duan.hday.repository.passenger;

import com.duan.hday.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy tất cả review của một người (được đánh giá)
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    // Tính điểm trung bình cộng
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double getAverageRating(@Param("userId") Long userId);

    boolean existsByTripIdAndReviewerIdAndRevieweeId(Long tripId, Long reviewerId, Long revieweeId);
}
