package com.innodealing.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import com.innodealing.domain.BondCcxeFinaSheetSyncBookMark;

@Component
public interface BondCcxeFinaSheetSyncRepo extends MongoRepository<BondCcxeFinaSheetSyncBookMark, String> {
}
