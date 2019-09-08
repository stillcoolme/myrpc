package com.stillcoolme.service;

public interface Invoker<T> {

    public T invoke(Object[] args);

    public void setResult(String result);
}
