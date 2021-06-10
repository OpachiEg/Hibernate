package model;

import annotations.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_table")
public class User {

    @Id
    @Column(name = "user_id")
    private Integer id;
    @Column(name = "login")
    private String login;
    @Column(name = "name")
    private String name;
    @Column(name = "surname")
    private String surname;
    @Column(name = "active")
    private Boolean active;
    @OneToOne
    @Column(name = "passport_id")
    private Passport passport;

}
