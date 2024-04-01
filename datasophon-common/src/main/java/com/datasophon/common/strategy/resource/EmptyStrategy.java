package com.datasophon.common.strategy.resource;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmptyStrategy extends ResourceStrategy {

    @Override
    public void exec() {

    }
}
