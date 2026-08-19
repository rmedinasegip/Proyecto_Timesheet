package com.llacsaa.timesheet.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tins_company")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TinsCompany {

    @EmbeddedId
    private TinsCompanyId id;

    private String name;

    private Long usercreate;

    private Long userlastmodify;

    private LocalDateTime datecreate;

    private LocalDateTime datemodify;
}
