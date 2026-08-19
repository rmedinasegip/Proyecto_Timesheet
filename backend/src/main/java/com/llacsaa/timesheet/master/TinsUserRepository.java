package com.llacsaa.timesheet.master;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TinsUserRepository extends JpaRepository<TinsUser, TinsUserId> {
    List<TinsUser> findByIdCodeinstanceAndIdCodecompanyOrderByIdCode(String codeinstance, String codecompany);

    Optional<TinsUser> findByIdCodeinstanceAndIdCodecompanyAndIdCode(String codeinstance, String codecompany, Long code);

    Optional<TinsUser> findByIdCodeinstanceAndIdCodecompanyAndEmail(String codeinstance, String codecompany, String email);

    List<TinsUser> findByPasswordhashIsNull();
}
