package com.project.login.repository;
import com.project.login.entity.QualityMasterEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QualityMasterRepository extends JpaRepository<QualityMasterEntity, Long> {

	List<QualityMasterEntity> findByUserId(Long userId);
	
	Optional<QualityMasterEntity> findByIdAndUserId(Long id, Long userId);
	
	void deleteByIdAndUserId(Long id, Long userId);
	
}
