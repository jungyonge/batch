package com.batch.controller.named.baseball;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Special {

    private FirstEvent firstHomerun;
    private FirstEvent firstStrikeOut;
    private FirstEvent firstBaseOnBall;
    // 각 필드에 대한 getter와 setter 필요
}
