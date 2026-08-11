package com.xinglin.user.repository;

import com.xinglin.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserAccount a set a.availablePoints = a.availablePoints + :points, " +
            "a.totalPoints = a.totalPoints + :points, a.version = a.version + 1 " +
            "where a.userId = :userId and a.version = :version")
    int addPointsCas(@Param("userId") Long userId, @Param("points") Integer points, @Param("version") Integer version);
}
