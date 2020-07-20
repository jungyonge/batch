package com.batch.mapper;

import com.batch.annotation.MasterDb;
import com.batch.model.BasketballModel;
import org.springframework.stereotype.Repository;

@MasterDb
@Repository
public interface BasketballMapper {

    public int insertBasketMatch(BasketballModel basketballModel);

    public int checkGameIdCount(BasketballModel basketballModel);

    public int updateBasketStat(BasketballModel basketballModel);
}
