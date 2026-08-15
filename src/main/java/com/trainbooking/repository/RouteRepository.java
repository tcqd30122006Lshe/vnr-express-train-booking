package com.trainbooking.repository;

import com.trainbooking.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,Long> {
    boolean existsByCode(String code);

    Optional<Route> findByCode(String code);

}
