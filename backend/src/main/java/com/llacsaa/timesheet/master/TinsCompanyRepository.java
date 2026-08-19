package com.llacsaa.timesheet.master;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TinsCompanyRepository extends JpaRepository<TinsCompany, TinsCompanyId> {
    List<TinsCompany> findByIdCodeinstanceOrderByIdCode(String codeinstance);
}
