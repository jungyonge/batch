package com.batch.mapper;

import com.batch.annotation.MasterDb;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper

public interface CommonMapper {

    List selectSportMakeExcelList();

    List selectMemberList();

    List selectSportMakeExcelListWithoutPitcher();
}
