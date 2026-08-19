package com.llacsaa.timesheet.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TinsCustomerId implements Serializable {
    private String codeinstance;
    private String codecompany;
    private Long code;
}
