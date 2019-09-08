package com.stillcoolme.service.utils;

import com.stillcoolme.service.registry.RegistryInfo;

import java.util.List;
import java.util.Random;

/**
 * @author: stillcoolme
 * @date: 2019/9/8 14:48
 * @description:
 */
public class RandomLoadbalancer implements LoadBalancer {

    @Override
    public RegistryInfo choose(List<RegistryInfo> registryInfos) {
        Random random = new Random();
        int index = random.nextInt(registryInfos.size());
        return registryInfos.get(index);
    }
}
