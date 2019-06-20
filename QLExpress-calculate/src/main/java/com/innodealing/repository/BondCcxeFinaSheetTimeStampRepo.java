package com.innodealing.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import com.innodealing.domain.BondCcxeFinaSheetSyncBookMark;
import com.innodealing.domain.BondFinaSheetTimestamp;

@Component
public interface BondCcxeFinaSheetTimeStampRepo extends MongoRepository<BondFinaSheetTimestamp, String> {
}
