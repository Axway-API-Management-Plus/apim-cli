package com.axway.apim.setup.impl;

public enum ResultHandler {
    JSON_EXPORTER(JsonAPIManagerSetupExporter.class),
    YAML_EXPORTER(YamlAPIManagerSetupExporter.class),
    CONSOLE_EXPORTER(ConsoleAPIManagerSetupExporter.class);

    private final Class<APIManagerSetupResultHandler> implClass;

    @SuppressWarnings({"rawtypes", "unchecked"})
    ResultHandler(Class clazz) {
        this.implClass = clazz;
    }

    public Class<APIManagerSetupResultHandler> getClazz() {
        return implClass;
    }
}
