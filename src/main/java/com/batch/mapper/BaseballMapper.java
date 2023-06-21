package com.batch.mapper;

import com.batch.model.BaseballModel;
import com.batch.model.FilterConditionModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
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
