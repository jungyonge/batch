package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BaseballModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@MasterDb
@Repository
public interface BaseballPitcherMapper {

    List selectBaseballPitcherStat();

    void insertBaseballPitcherStat(BaseballModel baseballModel);

    int checkGameIdCount(BaseballModel baseballModel);

    void updateBaseballPitcherStat(BaseballModel baseballModel);
}
