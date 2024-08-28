package io.github.weimin96.manager.client.demo.service;

import io.github.weimin96.manager.client.demo.entity.User;

import java.util.List;

/**
 * @author panwm
 * @since 2024/8/28 22:56
 */
public interface IUserService {

    List<User> getAllUsers();

    User getUserById(String id);
}
