package com.llacsaa.timesheet.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TinsCatalogueItemRepository extends JpaRepository<TinsCatalogueItem, TinsCatalogueItemId> {
    List<TinsCatalogueItem> findByIdCodeinstanceAndIdCodecatOrderByIdCodeitem(String codeinstance, String codecat);

    Optional<TinsCatalogueItem> findByIdCodeinstanceAndIdCodecatAndIdCodeitem(String codeinstance, String codecat, String codeitem);
}
