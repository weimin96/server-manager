package io.github.weimin96.manager.client.demo.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author panwm
 * @since 2024/8/28 22:50
 */
@Data
@Entity
@Table(name = "user", schema = "public")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private String id;

    private String name;

    private String email;

}
