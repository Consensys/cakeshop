package com.jpmorgan.cakeshop.repo;

import com.jpmorgan.cakeshop.model.NodeInfo;
import io.swagger.annotations.Api;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Api
@RestController
@RepositoryRestResource(collectionResourceRel = "nodes", path = "nodes")
@Repository
public interface NodeInfoRepository extends PagingAndSortingRepository<NodeInfo, Long> {
    Optional<NodeInfo> findByRpcUrlAndTransactionManagerUrl(String rpcUrl, String transactionManagerUrl);
}
