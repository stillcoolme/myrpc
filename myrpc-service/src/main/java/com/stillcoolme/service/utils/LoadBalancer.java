package com.stillcoolme.service.utils;

import com.stillcoolme.service.registry.RegistryInfo;

import java.util.List;

public interface LoadBalancer {

    /**
     * 选择一个生产者
     *
     * @param registryInfos 生产者列表
     * @return 选中的生产者
     */
    RegistryInfo choose(List<RegistryInfo> registryInfos);
}
