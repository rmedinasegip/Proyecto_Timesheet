package com.llacsaa.timesheet.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tins_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TinsUser {

    @EmbeddedId
    private TinsUserId id;

    private Long codeperson;

    private String email;
    private String passwordhash;
    private String rolecat;
    private String role;

    private Long usercreate;

    private Long userlastmodify;

    private LocalDateTime datecreate;

    private LocalDateTime datemodify;
}
