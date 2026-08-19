package com.llacsaa.timesheet.master;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TinsPersonRepository extends JpaRepository<TinsPerson, TinsPersonId> {
    List<TinsPerson> findByIdCodeinstanceAndIdCodecompanyOrderByIdCode(String codeinstance, String codecompany);

    Optional<TinsPerson> findByIdCodeinstanceAndIdCodecompanyAndIdCode(String codeinstance, String codecompany, Long code);
}
