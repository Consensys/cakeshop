package com.jpmorgan.cakeshop.repo;

import com.jpmorgan.cakeshop.model.ContractInfo;
import io.swagger.annotations.Api;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api
@RestController
@RepositoryRestResource(collectionResourceRel = "contracts", path = "contracts")
@Repository
public interface ContractRepository extends PagingAndSortingRepository<ContractInfo, String> {
    List<ContractInfo> findAll();
}
