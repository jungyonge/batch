package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BaseballModel;
import org.springframework.stereotype.Repository;

@MasterDb
@Repository
public interface BaseballMapper {

    public int insertBaseballMatch(BaseballModel baseballModel);

    public int checkGameIdCount(BaseballModel baseballModel);

    public void updateBaseballStat(BaseballModel baseballModel);
}
