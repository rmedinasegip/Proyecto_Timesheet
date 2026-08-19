package com.llacsaa.timesheet.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;

@RestController
public class CatalogController {

    private final TinsCatalogueRepository catalogueRepository;
    private final TinsCatalogueItemRepository catalogueItemRepository;

    public CatalogController(TinsCatalogueRepository catalogueRepository,
                              TinsCatalogueItemRepository catalogueItemRepository) {
        this.catalogueRepository = catalogueRepository;
        this.catalogueItemRepository = catalogueItemRepository;
    }

    @GetMapping("/api/catalogs")
    public List<TinsCatalogue> listCatalogs() {
        return catalogueRepository.findByIdCodeinstanceOrderByIdCodecat(CODEINSTANCE);
    }

    @GetMapping("/api/catalogs/{codecat}/items")
    public List<TinsCatalogueItem> listCatalogItems(@PathVariable String codecat) {
        return catalogueItemRepository.findByIdCodeinstanceAndIdCodecatOrderByIdCodeitem(CODEINSTANCE, codecat);
    }
}
