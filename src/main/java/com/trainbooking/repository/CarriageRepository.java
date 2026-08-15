package com.trainbooking.repository;

import com.trainbooking.entity.Carriage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarriageRepository extends JpaRepository<Carriage,Long> {

    boolean existsByTrainIdAndCarriageNumber(Long trainId, Integer carriageNumber);


}
