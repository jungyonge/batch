package com.batch.mapper;

import com.batch.annotation.MasterDb;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@MasterDb
public interface CommonMapper {

    public List selectSportMakeExcelList();
}
