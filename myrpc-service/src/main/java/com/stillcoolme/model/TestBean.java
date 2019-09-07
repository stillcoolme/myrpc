package com.stillcoolme.model;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 18:48
 * @description:
 **/
public class TestBean {

    private String name;
    private Integer age;

    public TestBean(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}