package com.stillcoolme.provider.registry;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 19:04
 * @description:
 **/
public class RegistryInfo {

    private String hostname;
    private String ip;
    private Integer port;

    public RegistryInfo(String hostname, String ip, Integer port) {
        this.hostname = hostname;
        this.ip = ip;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
