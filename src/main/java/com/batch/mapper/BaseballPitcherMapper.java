package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BaseballModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@MasterDb
@Repository
public interface BaseballPitcherMapper {

    public List selectBaseballPitcherStat();

    public int insertBaseballPitcherStat(BaseballModel baseballModel);

    public int checkGameIdCount(BaseballModel baseballModel);

}
