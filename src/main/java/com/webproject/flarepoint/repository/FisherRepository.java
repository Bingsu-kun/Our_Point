package com.webproject.flarepoint.repository;

import com.webproject.flarepoint.model.user.Fisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FisherRepository extends JpaRepository<Fisher, Long> {
    @Query(value = "SELECT * FROM fisher WHERE email = :email",nativeQuery = true)
    Fisher findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM fisher WHERE fisher_name = :fisherName", nativeQuery = true)
    Fisher findByName(@Param("fisherName") String fisherName);

}
