package com.llacsaa.timesheet.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tprj_proj_team")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqteam;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;

    private Long memberuser;
    private String projectrol;
    private LocalDate assignmentdate;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;
}
