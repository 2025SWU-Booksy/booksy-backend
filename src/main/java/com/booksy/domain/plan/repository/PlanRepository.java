package com.booksy.domain.plan.repository;

import com.booksy.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 플랜 정보를 관리하는 JPA 레포지토리 인터페이스
 */
public interface PlanRepository extends JpaRepository<Plan, Long> {

}
