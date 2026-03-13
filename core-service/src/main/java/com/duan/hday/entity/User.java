package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String email;
    private String avatarUrl;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isDeleted = false;

    /* ================= RELATIONS ================= */

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<AuthAccount> authAccounts;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private DriverProfile driverProfile;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<Vehicle> vehicles;

   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) // Bỏ orphanRemoval = true
    private List<UserDevice> devices;

    // Thêm vào trong class User
    @Builder.Default
    private Double averageRating = 5.0; // Mặc định cho 5 sao cho người mới

    @Builder.Default
    private Integer totalReviews = 0;

    // Trong class User
    public static User createNewSocialUser(String fullName, String email, String avatarUrl) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);
        user.setIsActive(true);
        user.setIsDeleted(false);
        return user;
    }
}
