package com.llacsaa.timesheet.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tins_catalogue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TinsCatalogue {

    @EmbeddedId
    private TinsCatalogueId id;

    private String name;

    private Long usercreate;

    private Long userlastmodify;

    private LocalDateTime datecreate;

    private LocalDateTime datemodify;
}
