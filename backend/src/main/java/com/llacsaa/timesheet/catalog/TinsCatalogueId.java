package com.llacsaa.timesheet.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TinsCatalogueId implements Serializable {
    private String codeinstance;
    private String codecat;
}
