package com.llacsaa.timesheet.master;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;

@RestController
public class MasterController {

    private final TinsCompanyRepository companyRepository;
    private final TinsPersonRepository personRepository;
    private final TinsCustomerRepository customerRepository;
    private final TinsUserRepository userRepository;

    public MasterController(TinsCompanyRepository companyRepository,
                             TinsPersonRepository personRepository,
                             TinsCustomerRepository customerRepository,
                             TinsUserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/companies")
    public List<TinsCompany> listCompanies() {
        return companyRepository.findByIdCodeinstanceOrderByIdCode(CODEINSTANCE);
    }

    @GetMapping("/api/persons")
    public List<TinsPerson> listPersons() {
        return personRepository.findByIdCodeinstanceAndIdCodecompanyOrderByIdCode(CODEINSTANCE, CODECOMPANY);
    }

    @GetMapping("/api/customers")
    public List<CustomerView> listCustomers() {
        Map<Long, String> personNamesByCode = personRepository
                .findByIdCodeinstanceAndIdCodecompanyOrderByIdCode(CODEINSTANCE, CODECOMPANY)
                .stream()
                .collect(Collectors.toMap(p -> p.getId().getCode(), TinsPerson::getName));

        return customerRepository.findByIdCodeinstanceAndIdCodecompanyOrderByIdCode(CODEINSTANCE, CODECOMPANY)
                .stream()
                .map(c -> new CustomerView(c.getId().getCode(), personNamesByCode.get(c.getCodeperson())))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/users")
    public List<UserView> listUsers() {
        Map<Long, String> personNamesByCode = personRepository
                .findByIdCodeinstanceAndIdCodecompanyOrderByIdCode(CODEINSTANCE, CODECOMPANY)
                .stream()
                .collect(Collectors.toMap(p -> p.getId().getCode(), TinsPerson::getName));

        return userRepository.findByIdCodeinstanceAndIdCodecompanyOrderByIdCode(CODEINSTANCE, CODECOMPANY)
                .stream()
                .map(u -> new UserView(u.getId().getCode(), personNamesByCode.get(u.getCodeperson())))
                .collect(Collectors.toList());
    }
}
