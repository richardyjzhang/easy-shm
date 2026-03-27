package com.easyshm.repository;

import com.easyshm.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByLoginNameContaining(String loginName, Pageable pageable);

    boolean existsByLoginName(String loginName);

    boolean existsByLoginNameAndIdNot(String loginName, Long id);
}
