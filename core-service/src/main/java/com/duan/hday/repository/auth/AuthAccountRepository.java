package com.duan.hday.repository.auth;


import com.duan.hday.entity.AuthAccount;
import com.duan.hday.entity.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {

    // Thêm Query này để nạp luôn User, tránh Lazy Loading bị null ở Controller
    @Query("SELECT a FROM AuthAccount a JOIN FETCH a.user WHERE a.provider = :provider AND a.identifier = :identifier")
    Optional<AuthAccount> findByProviderAndIdentifierWithUser(AuthProvider provider, String identifier);
    
    Optional<AuthAccount> findByProviderAndIdentifier(AuthProvider provider, String identifier);
    Optional<AuthAccount> findByIdentifier(String identifier);
    Optional<AuthAccount> findByIdentifierAndProvider(String identifier, AuthProvider provider);
}
