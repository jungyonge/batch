package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BaseballModel;
import com.batch.model.FilterConditionModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@MasterDb
@Repository
public interface BaseballMapper {

   void insertBaseballMatch(BaseballModel baseballModel);

   int checkGameIdCount(BaseballModel baseballModel);

   void updateBaseballStat(BaseballModel baseballModel);

   List selectBaseballStat();

   void insertBaseballSummary(FilterConditionModel filterConditionModel);

   void truncateBaseballSummary();

   void updateBaseballNextMatchStat(BaseballModel baseballModel);

   List selectBaseballAllSummary();

}
