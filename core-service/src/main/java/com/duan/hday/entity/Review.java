package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;
import com.duan.hday.entity.enums.ReviewType; // Enum: PASSENGER_REVIEW_DRIVER, DRIVER_REVIEW_PASSENGER


@Entity
@Table(name = "reviews")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @Column(nullable = false)
    private Integer rating; // 1-5 sao

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    private ReviewType type;
}