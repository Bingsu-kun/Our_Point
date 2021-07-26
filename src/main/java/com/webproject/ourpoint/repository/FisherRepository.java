package com.webproject.ourpoint.repository;

import com.webproject.ourpoint.model.user.Fisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FisherRepository extends JpaRepository<Fisher, Long> {
    @Query(value = "SELECT * FROM fisher WHERE email = :email",nativeQuery = true)
    Fisher findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM fisher WHERE fishername = :fishername", nativeQuery = true)
    Fisher findByName(@Param("fishername") String fishername);

}
