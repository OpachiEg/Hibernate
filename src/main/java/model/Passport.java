package model;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import annotations.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "passport_table")
public class Passport {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

}
