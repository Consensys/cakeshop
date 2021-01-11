package com.jpmorgan.cakeshop.repo;

import com.jpmorgan.cakeshop.model.Transaction;
import io.swagger.annotations.Api;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api
@RestController
@RepositoryRestResource(collectionResourceRel = "transactions", path = "transactions")
@Repository
public interface TransactionRepository extends PagingAndSortingRepository<Transaction, String> {
    List<Transaction> findAllByTo(String address);

    Page<Transaction> findAllByToOrFrom(Pageable pageable, String to, String from);

    Transaction findByContractAddress(String address);
}
