package com.trainbooking.repository;

import com.trainbooking.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train,Long> {

    boolean existsByCode(String code);

    Optional<Train> findByCode(String code);
}
