package com.datasophon.worker.test;

import com.datasophon.common.model.Generators;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.worker.utils.FreemakerUtils;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <pre>
 *
 * @author zhenqin
 */
public class MutilTemplateFileTest {

    @Test
    public void generateCustomTemplate() throws IOException, TemplateException {
        Generators generators = new Generators();
        generators.setConfigFormat("custom");
        generators.setFilename("application.yml");
        generators.setTemplateName("dolphinscheduler_env.ftl");
        generators.setOutputDirectory("./");

        // config data
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setConfigType("map");
        serviceConfig.setName("flowschedulerUrl");
        serviceConfig.setValue("var1");

        ServiceConfig serviceConfig2 = new ServiceConfig();
        serviceConfig2.setConfigType("map");
        serviceConfig2.setName("flowschedulerToken");
        serviceConfig2.setValue("var2");

        ArrayList<ServiceConfig> serviceConfigs = new ArrayList<>();
        serviceConfigs.add(serviceConfig);
        serviceConfigs.add(serviceConfig2);

        FreemakerUtils.generateConfigFile(generators, serviceConfigs, "", "mytemplates");
    }
}
