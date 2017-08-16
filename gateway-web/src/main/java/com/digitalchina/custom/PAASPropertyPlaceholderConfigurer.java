package com.digitalchina.custom;

import com.digitalchina.custom.util.ConfigUtil;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by yangzhec on 2016/12/12.
 */
public class PAASPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
    @Override
    protected Properties mergeProperties() throws IOException {
        return ConfigUtil.loadFromPaasConfigCenter();
    }
}
