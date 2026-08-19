package com.llacsaa.timesheet.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tprj_project_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seq;

    private String contractNumber;
    private String projectCode;
    private String projectName;
    private String projectDescription;
    private BigDecimal projectDuration;

    private Long codecustomer;
    private Long codeuserPm;

    private LocalDate requestDate;
    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate realStartDate;
    private LocalDate realEndDate;

    private String statuscat;
    private String status;

    private LocalDate lastcutoffdate;
    private BigDecimal advexpectedperc;
    private BigDecimal advrealperc;
    private BigDecimal advexpecteddays;
    private BigDecimal advrealdays;
    private BigDecimal daysconsumed;
    private BigDecimal varadvplannedperc;
    private BigDecimal efectivityperc;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;

    public static TprjProjectHis snapshotOf(TprjProject p, String actiondml, long userchange) {
        TprjProjectHis h = new TprjProjectHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(p.getCodeinstance());
        h.setCodecompany(p.getCodecompany());
        h.setSeq(p.getSeq());
        h.setContractNumber(p.getContractNumber());
        h.setProjectCode(p.getProjectCode());
        h.setProjectName(p.getProjectName());
        h.setProjectDescription(p.getProjectDescription());
        h.setProjectDuration(p.getProjectDuration());
        h.setCodecustomer(p.getCodecustomer());
        h.setCodeuserPm(p.getCodeuserPm());
        h.setRequestDate(p.getRequestDate());
        h.setBaseStartDate(p.getBaseStartDate());
        h.setBaseEndDate(p.getBaseEndDate());
        h.setPlannedStartDate(p.getPlannedStartDate());
        h.setPlannedEndDate(p.getPlannedEndDate());
        h.setRealStartDate(p.getRealStartDate());
        h.setRealEndDate(p.getRealEndDate());
        h.setStatuscat(p.getStatuscat());
        h.setStatus(p.getStatus());
        h.setLastcutoffdate(p.getLastcutoffdate());
        h.setAdvexpectedperc(p.getAdvexpectedperc());
        h.setAdvrealperc(p.getAdvrealperc());
        h.setAdvexpecteddays(p.getAdvexpecteddays());
        h.setAdvrealdays(p.getAdvrealdays());
        h.setDaysconsumed(p.getDaysconsumed());
        h.setVaradvplannedperc(p.getVaradvplannedperc());
        h.setEfectivityperc(p.getEfectivityperc());
        h.setUsercreate(p.getUsercreate());
        h.setUserlastmodify(p.getUserlastmodify());
        h.setDatecreate(p.getDatecreate());
        h.setDatemodify(p.getDatemodify());
        return h;
    }
}
