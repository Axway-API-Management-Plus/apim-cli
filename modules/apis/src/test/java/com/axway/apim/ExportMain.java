package com.axway.apim;

import com.axway.apim.cli.CLIServiceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExportMain {

    private static final Logger LOG = LoggerFactory.getLogger(ExportMain.class);


    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        if(args.length == 0) {
            System.out.println("Invalid param");
            return;
        }
        String serviceName = args[1];
        if (serviceName == null) {
            System.out.println("Invalid arguments - prefix commandline param with \"api get\"");
            return;
        }
        for (final Method method : APIExportApp.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CLIServiceMethod.class)) {
                CLIServiceMethod cliServiceMethod = method.getAnnotation(CLIServiceMethod.class);
                String name = cliServiceMethod.name();
                if (serviceName.equals(name)) {
                    LOG.info("Calling Operation " + method.getName());
                    int rc = (int) method.invoke(null, (Object) args);
                    System.exit(rc);
                }
            }
        }
        LOG.info("No matching method");
    }
}
