package com.yy.hawk4j.common.design.builder;

import java.io.Serializable;

/**
 * Builder pattern interface definition.
 */
public interface Builder<T> extends Serializable {

    T build();

}
