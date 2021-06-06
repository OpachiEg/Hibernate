package model;

import annotations.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_table")
public class User {

    @Id
    @Column(name = "user_id", nullable = false)
    private Integer id;
    @Column(name = "login", nullable = false)
    private String login;
    @Column(name = "active", nullable = false)
    private Boolean active;

}
