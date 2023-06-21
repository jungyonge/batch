package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.VolleyballModel;
import com.batch.model.FilterConditionModel;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface VolleyballMapper {

   void insertVolleyballMatch(VolleyballModel volleyballModel);

   int checkGameIdCount(VolleyballModel volleyballModel);

   void updateVolleyballStat(VolleyballModel volleyballModel);

   List selectVolleyballStat();

   void insertVolleyballSummary(FilterConditionModel filterConditionModel);

   void truncateVolleyballSummary();

   void updateVolleyballNextMatchStat(VolleyballModel volleyballModel);

   List selectVolleyballAvgPoint();

}
