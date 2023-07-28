package io.nop.spring.delta.mybatis;

import org.apache.ibatis.binding.MapperProxy;
import org.apache.ibatis.binding.MapperProxyFactory;

import java.lang.reflect.Proxy;

public class MapperProxyFactoryEx<T> extends MapperProxyFactory<T> {
    private final Class<?> mapperTypeEx;

    public MapperProxyFactoryEx(Class<T> mapperInterface, Class<?> mapperTypeEx) {
        super(mapperInterface);
        this.mapperTypeEx = mapperTypeEx;
    }

    @Override
    protected T newInstance(MapperProxy<T> mapperProxy) {
        Class<?> inf = mapperTypeEx;
        if (inf == null)
            inf = getMapperInterface();
        return (T) Proxy.newProxyInstance(getMapperInterface().getClassLoader(), new Class[]{inf}, mapperProxy);
    }
}
