package com.easyshm.service;

import com.easyshm.entity.User;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<User> list(String loginName, int page, int size);

    User getById(Long id);

    User create(User user);

    User update(Long id, User user);

    void resetPassword(Long id, String preHashedPassword);

    void delete(Long id);
}
