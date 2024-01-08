package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Config;
import com.axway.apim.lib.APIManagerConfigAnnotation;
import com.axway.apim.lib.APIManagerConfigAnnotation.ConfigType;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;

import java.lang.reflect.Field;

public class ConsolePrinterConfig {

    APIManagerAdapter adapter;

    StandardExportParams params;

    ConfigType[] standardFields = new ConfigType[]{
        ConfigType.APIManager,
        ConfigType.APIPortal,
        ConfigType.General,
        ConfigType.APIRegistration
    };
    ConfigType[] wideFields = new ConfigType[]{
        ConfigType.APIManager,
        ConfigType.APIPortal,
        ConfigType.General,
        ConfigType.APIRegistration,
        ConfigType.APIImport,
        ConfigType.Delegation,
        ConfigType.GlobalPolicies,
        ConfigType.FaultHandlers
    };

    ConfigType[] ultraFields = new ConfigType[]{
        ConfigType.APIManager,
        ConfigType.APIPortal,
        ConfigType.General,
        ConfigType.APIRegistration,
        ConfigType.APIImport,
        ConfigType.Delegation,
        ConfigType.GlobalPolicies,
        ConfigType.FaultHandlers,
        ConfigType.Session,
        ConfigType.AdvisoryBanner,
    };

    public ConsolePrinterConfig(StandardExportParams params) throws AppException {
        this.params = params;
        try {
            adapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new AppException("Unable to get APIManagerAdapter", ErrorCode.UNXPECTED_ERROR);
        }
    }

    public void export(Config config) {
        Console.println();
        Console.println("Configuration for: '" + config.getPortalName() + "' Version: " + config.getProductVersion());
        Console.println();
        switch (params.getWide()) {
            case standard:
                print(config, standardFields);
                break;
            case wide:
                print(config, wideFields);
                break;
            case ultra:
                print(config, ultraFields);
        }
    }

    private void print(Config config, ConfigType[] configTypes) {
        for (ConfigType configType : configTypes) {
            Console.println(configType.getClearName() + ":");
            Field[] fields = Config.class.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(APIManagerConfigAnnotation.class)) {
                    APIManagerConfigAnnotation annotation = field.getAnnotation(APIManagerConfigAnnotation.class);
                    if (annotation.configType() == configType) {
                        String dots = ".....................................";
                        Console.printf("%s %s: %s", annotation.name(), dots.substring(annotation.name().length()), Utils.getFieldValue(field.getName(), config));
                    }
                }
            }
            Console.println();
        }
    }
}
