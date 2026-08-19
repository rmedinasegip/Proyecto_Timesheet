package com.llacsaa.timesheet.master;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TinsCustomerRepository extends JpaRepository<TinsCustomer, TinsCustomerId> {
    List<TinsCustomer> findByIdCodeinstanceAndIdCodecompanyOrderByIdCode(String codeinstance, String codecompany);

    Optional<TinsCustomer> findByIdCodeinstanceAndIdCodecompanyAndIdCode(String codeinstance, String codecompany, Long code);
}
