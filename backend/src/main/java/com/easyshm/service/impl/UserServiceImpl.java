package com.easyshm.service.impl;

import com.easyshm.entity.User;
import com.easyshm.repository.DepartmentRepository;
import com.easyshm.repository.UserRepository;
import com.easyshm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<User> list(String loginName, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (StringUtils.hasText(loginName)) {
            return userRepository.findByLoginNameContaining(loginName.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在（id=" + id + "）"));
    }

    @Override
    public User create(User user) {
        if (!StringUtils.hasText(user.getLoginName())) {
            throw new RuntimeException("登录名不能为空");
        }
        if (userRepository.existsByLoginName(user.getLoginName().trim())) {
            throw new RuntimeException("登录名已存在");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
        if (!departmentRepository.existsById(user.getDepartmentId())) {
            throw new RuntimeException("机构不存在（id=" + user.getDepartmentId() + "）");
        }

        user.setId(null);
        user.setLoginName(user.getLoginName().trim());
        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setPassword(null);

        if (user.getRole() == null) {
            user.setRole(3);
        }

        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User incoming) {
        User existing = getById(id);
        if (!StringUtils.hasText(incoming.getLoginName())) {
            throw new RuntimeException("登录名不能为空");
        }
        String newName = incoming.getLoginName().trim();
        if (!newName.equals(existing.getLoginName()) && userRepository.existsByLoginNameAndIdNot(newName, id)) {
            throw new RuntimeException("登录名已存在");
        }
        if (!departmentRepository.existsById(incoming.getDepartmentId())) {
            throw new RuntimeException("机构不存在（id=" + incoming.getDepartmentId() + "）");
        }

        existing.setLoginName(newName);
        existing.setDepartmentId(incoming.getDepartmentId());
        existing.setRole(incoming.getRole() != null ? incoming.getRole() : 3);
        existing.setPhone(incoming.getPhone());
        existing.setAddress(incoming.getAddress());
        existing.setRemark(incoming.getRemark());

        return userRepository.save(existing);
    }

    @Override
    public void resetPassword(Long id, String preHashedPassword) {
        if (!StringUtils.hasText(preHashedPassword)) {
            throw new RuntimeException("密码不能为空");
        }
        User existing = getById(id);
        existing.setPasswordHash(passwordEncoder.encode(preHashedPassword));
        userRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在（id=" + id + "）");
        }
        userRepository.deleteById(id);
    }
}
