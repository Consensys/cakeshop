package com.jpmorgan.cakeshop.repo;

import com.jpmorgan.cakeshop.model.Event;
import io.swagger.annotations.Api;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RepositoryRestResource(collectionResourceRel = "events", path = "events")
@Repository
public interface EventRepository extends PagingAndSortingRepository<Event, String> {
}
