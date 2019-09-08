package com.stillcoolme.service.utils;

import com.stillcoolme.service.registry.RegistryInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: stillcoolme
 * @date: 2019/9/8 14:50
 * @description:
 */
public class RoundRobinLoadbalancer implements LoadBalancer {
    AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public RegistryInfo choose(List<RegistryInfo> registryInfos) {
        if(atomicInteger.get() == registryInfos.size()){
            atomicInteger.set(0);
        }
        return registryInfos.get(atomicInteger.getAndIncrement());
    }
}
