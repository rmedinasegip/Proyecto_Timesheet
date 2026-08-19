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
@Table(name = "tprj_proj_team_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjTeamHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;
    private Long seqteam;

    private Long memberuser;
    private String projectrol;
    private LocalDate assignmentdate;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;

    public static TprjProjTeamHis snapshotOf(TprjProjTeam t, String actiondml, long userchange) {
        TprjProjTeamHis h = new TprjProjTeamHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(t.getCodeinstance());
        h.setCodecompany(t.getCodecompany());
        h.setSeqproject(t.getSeqproject());
        h.setSeqteam(t.getSeqteam());
        h.setMemberuser(t.getMemberuser());
        h.setProjectrol(t.getProjectrol());
        h.setAssignmentdate(t.getAssignmentdate());
        h.setUsercreate(t.getUsercreate());
        h.setUserlastmodify(t.getUserlastmodify());
        h.setDatecreate(t.getDatecreate());
        h.setDatemodify(t.getDatemodify());
        return h;
    }
}
