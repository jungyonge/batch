package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BaseballModel;

@MasterDb
public interface BaseballMapper {

    public int insertBaseballMatch(BaseballModel baseballModel);

}
