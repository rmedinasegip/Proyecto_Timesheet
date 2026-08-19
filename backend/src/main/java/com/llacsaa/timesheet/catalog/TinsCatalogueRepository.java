package com.llacsaa.timesheet.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TinsCatalogueRepository extends JpaRepository<TinsCatalogue, TinsCatalogueId> {
    List<TinsCatalogue> findByIdCodeinstanceOrderByIdCodecat(String codeinstance);
}
