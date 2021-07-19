package com.webproject.ourpoint.repository;

import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.user.ConnectedUser;
import com.webproject.ourpoint.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM user u WHERE u.user_email = :user_email")
    Optional<User> findByEmail(@Param("user_email") String email);

    @Query("SELECT u FROM user u WHERE u.user_id = :user_id")
    List<ConnectedUser> findAllConnectedUser(@Param("user_id") Long userId);

    @Query("SELECT u FROM user u WHERE u.user_id = :user_id")
    List<Id<User, Long>> findConnectedIds(@Param("user_id") Long value);
}
