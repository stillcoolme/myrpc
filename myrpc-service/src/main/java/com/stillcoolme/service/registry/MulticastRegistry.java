package com.stillcoolme.service.registry;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 19:30
 * @description:
 **/
public class MulticastRegistry implements Registry {
    public MulticastRegistry(String registryUrl) {

    }

    @Override
    public void register(Class clazz, RegistryInfo registryInfo) throws Exception {

    }

    @Override
    public List fetchRegistry(Class clazz) throws Exception {
        return null;
    }
}
