package com.batch.controller.named.baseball.gamehistory;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Away {

    public int id;
    public String name;
    public String shortName;
    public String imgPath;
    public ArrayList<Pitching> pitchings;
    public ArrayList<Batter> batters;
}
