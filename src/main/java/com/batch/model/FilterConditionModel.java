package com.batch.model;

import lombok.Data;

@Data
public class FilterConditionModel {
    boolean all = false;
    boolean ground  = false;
    boolean week    = false;
    boolean odd = false;
    boolean rest    = false;
    boolean pitcher = false;
}
