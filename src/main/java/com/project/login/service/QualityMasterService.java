package com.project.login.service;

import com.project.login.entity.QualityMasterEntity;
import com.project.login.repository.QualityMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class QualityMasterService {
	
	private final QualityMasterRepository repository;
	
	public QualityMasterService(QualityMasterRepository repository) {
		this.repository = repository;
	}
	
	public QualityMasterEntity save(QualityMasterEntity qtEntity) {
		return repository.save(qtEntity);		
		
	}
	
	public List<QualityMasterEntity> findByUser(Long userId) {
        return repository.findByUserId(userId);
    }
	
	public Optional<QualityMasterEntity> findByIdAndUser(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId);
    }
	
	@Transactional
	public void delete(Long id, Long userId) {
	     repository.deleteByIdAndUserId(id, userId);
	}
}
