package com.llacsaa.timesheet.common;

import com.llacsaa.timesheet.catalog.TinsCatalogueItemRepository;
import com.llacsaa.timesheet.master.TinsCompanyRepository;
import com.llacsaa.timesheet.master.TinsCustomerRepository;
import com.llacsaa.timesheet.master.TinsPersonRepository;
import com.llacsaa.timesheet.master.TinsUserRepository;
import org.springframework.stereotype.Component;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;

/**
 * Resuelve nombres/etiquetas para mostrar en pantalla a partir de los
 * códigos guardados en las tablas tprj_*, sin duplicar el join en cada
 * servicio de módulo.
 */
@Component
public class NameResolver {

    private final TinsPersonRepository personRepository;
    private final TinsCustomerRepository customerRepository;
    private final TinsUserRepository userRepository;
    private final TinsCatalogueItemRepository catalogueItemRepository;
    private final TinsCompanyRepository companyRepository;

    public NameResolver(TinsPersonRepository personRepository,
                         TinsCustomerRepository customerRepository,
                         TinsUserRepository userRepository,
                         TinsCatalogueItemRepository catalogueItemRepository,
                         TinsCompanyRepository companyRepository) {
        this.personRepository = personRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.catalogueItemRepository = catalogueItemRepository;
        this.companyRepository = companyRepository;
    }

    public String companyName(String code) {
        if (code == null) {
            return null;
        }
        return companyRepository.findById(new com.llacsaa.timesheet.master.TinsCompanyId(CODEINSTANCE, code))
                .map(com.llacsaa.timesheet.master.TinsCompany::getName)
                .orElse(null);
    }

    public String customerName(Long codecustomer) {
        if (codecustomer == null) {
            return null;
        }
        return customerRepository.findByIdCodeinstanceAndIdCodecompanyAndIdCode(CODEINSTANCE, CODECOMPANY, codecustomer)
                .flatMap(c -> personRepository.findByIdCodeinstanceAndIdCodecompanyAndIdCode(CODEINSTANCE, CODECOMPANY, c.getCodeperson()))
                .map(com.llacsaa.timesheet.master.TinsPerson::getName)
                .orElse(null);
    }

    public String userName(Long codeuser) {
        if (codeuser == null) {
            return null;
        }
        return userRepository.findByIdCodeinstanceAndIdCodecompanyAndIdCode(CODEINSTANCE, CODECOMPANY, codeuser)
                .flatMap(u -> personRepository.findByIdCodeinstanceAndIdCodecompanyAndIdCode(CODEINSTANCE, CODECOMPANY, u.getCodeperson()))
                .map(com.llacsaa.timesheet.master.TinsPerson::getName)
                .orElse(null);
    }

    public String catalogItemName(String codecat, String codeitem) {
        if (codecat == null || codeitem == null) {
            return null;
        }
        return catalogueItemRepository.findByIdCodeinstanceAndIdCodecatAndIdCodeitem(CODEINSTANCE, codecat, codeitem)
                .map(com.llacsaa.timesheet.catalog.TinsCatalogueItem::getName)
                .orElse(null);
    }
}
