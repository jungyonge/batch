package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.FilterConditionModel;
import com.batch.model.HockeyModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@MasterDb
@Repository
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
