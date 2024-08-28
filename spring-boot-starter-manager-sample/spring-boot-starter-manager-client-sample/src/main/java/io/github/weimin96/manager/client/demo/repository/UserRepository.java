package io.github.weimin96.manager.client.demo.repository;

import io.github.weimin96.manager.client.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author panwm
 * @since 2024/8/28 22:57
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
