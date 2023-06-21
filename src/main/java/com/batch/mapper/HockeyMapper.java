package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.FilterConditionModel;
import com.batch.model.HockeyModel;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface HockeyMapper {

   void insertHockeyMatch(HockeyModel hockeyModel);

   int checkGameIdCount(HockeyModel hockeyModel);

   void updateHockeyStat(HockeyModel hockeyModel);

   List selectHockeyStat();

   void insertHockeySummary(FilterConditionModel filterConditionModel);

   void truncateHockeySummary();

   void updateHockeyNextMatchStat(HockeyModel hockeyModel);

   List selectHockeyAvgPoint();

}
