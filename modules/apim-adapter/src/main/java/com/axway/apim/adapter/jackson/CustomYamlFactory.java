package com.axway.apim.adapter.jackson;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.yaml.snakeyaml.LoaderOptions;

public final class CustomYamlFactory {

    private CustomYamlFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    private static YAMLFactory yamlFactory;

    public static YAMLFactory createYamlFactory() {
        if (yamlFactory == null) {
            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setCodePointLimit(10 * 1024 * 1024); // 10 MB
            yamlFactory = YAMLFactory.builder().enable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).loaderOptions(loaderOptions).build();
        }
        return yamlFactory;
    }
}
