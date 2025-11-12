package com.jinjinjara.pola.rag.util;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encoded) throws IOException {
        Resource resource = encoded.getResource();

        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource);           // 여러 리소스도 처리하려면 배열 전달 가능
        Properties props = factory.getObject();   // null 가능성 대비
        if (props == null) props = new Properties();

        String sourceName = (name != null && !name.isBlank())
                ? name
                : resource.getDescription();      // filename null 대비
        return new PropertiesPropertySource(sourceName, props);
    }
}
