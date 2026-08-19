package com.llacsaa.timesheet.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Sin columna propia de nombre: el nombre del cliente se resuelve vía
 * {@code codeperson} -> {@link TinsPerson#getName()} (confirmado en
 * "Pantalla CRUD de Proyecto y Otros.docx").
 */
@Entity
@Table(name = "tins_customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TinsCustomer {

    @EmbeddedId
    private TinsCustomerId id;

    private Long codeperson;

    private Long usercreate;

    private Long userlastmodify;

    private LocalDateTime datecreate;

    private LocalDateTime datemodify;
}
