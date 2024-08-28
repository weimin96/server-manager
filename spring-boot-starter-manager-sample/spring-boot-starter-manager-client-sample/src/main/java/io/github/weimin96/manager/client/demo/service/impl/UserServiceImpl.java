package io.github.weimin96.manager.client.demo.service.impl;

import io.github.weimin96.manager.client.demo.entity.User;
import io.github.weimin96.manager.client.demo.repository.UserRepository;
import io.github.weimin96.manager.client.demo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author panwm
 * @since 2024/8/28 23:34
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }
}
