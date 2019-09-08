package com.stillcoolme.consumer;

/**
 * @author: stillcoolme
 * @date: 2019/9/8 12:00
 * @description:
 */
public class ReferenceConfig {

    private Class type;

    public ReferenceConfig(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}
