package com.traderalerting.repository;

import com.traderalerting.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query(value = "SELECT u.* FROM app_users u " +
            "JOIN user_favorites uf ON u.id = uf.user_id " +
            "JOIN symbols s ON uf.symbol_id = s.id " +
            "WHERE s.ticker = :ticker", nativeQuery = true)
    List<User> findUsersByFavoriteTicker(@Param("ticker") String ticker);
}
