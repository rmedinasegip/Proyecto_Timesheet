package com.llacsaa.timesheet.timesheet;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Filtros opcionales para GET /api/timesheets, construidos como
 * Specifications en vez de un JPQL con "(:param IS NULL OR ...)": ese patrón
 * dispara "could not determine data type of parameter" en Postgres/JDBC
 * cuando un parámetro solo aparece dentro de una comparación IS NULL en
 * TODAS sus apariciones dentro de la consulta (el driver no puede inferir su
 * tipo). Con Specifications, un filtro no provisto simplemente no agrega
 * predicado a la consulta.
 */
final class TimesheetSpecifications {

    private TimesheetSpecifications() {
    }

    static Specification<TprjProjectTimesheet> context(String codeinstance, String codecompany) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("codeinstance"), codeinstance),
                cb.equal(root.get("codecompany"), codecompany)
        );
    }

    static Specification<TprjProjectTimesheet> memberuser(Long memberuser) {
        return (root, query, cb) -> memberuser == null ? null : cb.equal(root.get("memberuser"), memberuser);
    }

    static Specification<TprjProjectTimesheet> dateFrom(LocalDate dateFrom) {
        return (root, query, cb) -> dateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("tsdate"), dateFrom);
    }

    static Specification<TprjProjectTimesheet> dateTo(LocalDate dateTo) {
        return (root, query, cb) -> dateTo == null ? null : cb.lessThanOrEqualTo(root.get("tsdate"), dateTo);
    }

    static Specification<TprjProjectTimesheet> status(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    static Specification<TprjProjectTimesheet> seqproject(Long seqproject) {
        return (root, query, cb) -> seqproject == null ? null : cb.equal(root.get("seqproject"), seqproject);
    }
}
