package com.trainbooking.repository;

import com.trainbooking.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    boolean existsByCode(String code);

    Optional<Station> findByCode(String code);
}
