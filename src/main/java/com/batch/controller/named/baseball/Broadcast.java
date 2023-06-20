package com.batch.controller.named.baseball;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Broadcast {

    private Score score;
    private String playText;
    private String locationType;
    private String eventType;
    private String displayTime;
    private int period;
    // 각 필드에 대한 getter와 setter 필요
}
