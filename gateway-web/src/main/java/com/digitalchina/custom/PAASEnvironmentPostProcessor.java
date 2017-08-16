package com.digitalchina.custom;

import com.digitalchina.custom.util.ConfigUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yangzhec on 2016/12/8.
 * 将这个类配置在META-INF/spring.factories中就可以在启动是加载，执行postProcessEnvironment方法
 */
public class PAASEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
        String str = "PAASConfigs";
        Map<String,Object> map = new HashMap<String,Object>();
        //此处从配置中心获取所有配置，添加到当前应用的环境中
        //使用这些配置有两个类，一个是FileAProps.java一个是FileBProps.java，
        // FileAProps是使用@Value注解注入，FileBProps是使用属性名注入
//        File f = new File("/usr/local/tomcat/logs/info.log");
//        if(f.exists()){
//            f.delete();
//        }
//        try {
//            f.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            System.setOut(new PrintStream(new FileOutputStream(f)));
//            System.setErr(System.out);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        Properties prop = ConfigUtil.loadFromPaasConfigCenter();
        for(Map.Entry<Object,Object> entry:prop.entrySet()){
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            map.put(key,value);
        }
        MapPropertySource mps =  new MapPropertySource(str, map);
        MutablePropertySources sources = configurableEnvironment.getPropertySources();
        String name = findPropertySource(sources);
        if (sources.contains(name)) {
            sources.addBefore(name, mps);
        }
        else {
            sources.addFirst(mps);
        }
    }

    private String findPropertySource(MutablePropertySources sources) {
        return "PAAS.CONFIG";
    }
}
