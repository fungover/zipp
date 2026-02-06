package org.fungover.zipp.repository;

import org.fungover.zipp.entity.HealthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthRepository extends JpaRepository<HealthEntity, Long> {}
