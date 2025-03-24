package com.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reservation.domain.ReviewImage;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

}
