package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BaseballModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface BaseballPitcherMapper {

    List<HashMap<Objects, Objects>> selectBaseballPitcherStat();

    void insertBaseballPitcherStat(BaseballModel baseballModel);

    int checkGameIdCount(BaseballModel baseballModel);

    void updateBaseballPitcherStat(BaseballModel baseballModel);
}
