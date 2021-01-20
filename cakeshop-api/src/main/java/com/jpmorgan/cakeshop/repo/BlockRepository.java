package com.jpmorgan.cakeshop.repo;

import com.jpmorgan.cakeshop.model.Block;
import io.swagger.annotations.Api;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Optional;

@Api
@RestController
@RepositoryRestResource(collectionResourceRel = "blocks", path = "blocks")
@Repository
public interface BlockRepository extends PagingAndSortingRepository<Block, String> {
    Optional<Block> findByNumber(@Param("number") BigInteger number);

    Block findTopByOrderByTimestampDesc();
}
