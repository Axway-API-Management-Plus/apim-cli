package com.axway.apim.config;

import com.axway.apim.config.model.GenerateTemplateParameters;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class GenerateTemplateCLIOptions extends CLIOptions {

    private GenerateTemplateCLIOptions(String[] args) {
        super(args);
    }

    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new GenerateTemplateCLIOptions(args);
        cliOptions = new StandardExportCLIOptions(cliOptions);
        cliOptions = new CoreCLIOptions(cliOptions);
        cliOptions.addOptions();
        cliOptions.parse();
        return cliOptions;
    }


    @Override
    public void addOptions() {
        // Define command line options required for Application export
        Option option = new Option("a", "apidefinition", true, "(Optional) The API Specification either as OpenAPI (JSON/YAML) or a WSDL for SOAP-Services:\n"
                + "- in local filesystem using a relative or absolute path. Example: swagger_file.json\n"
                + "  Please note: Local filesystem is not supported for WSDLs. Please use direct URL or a URL-Reference-File.\n"
                + "- a URL providing the Swagger-File or WSDL-File. Examples:\n"
                + "  [username/password@]https://any.host.com/my/path/to/swagger.json\n"
                + "  [username/password@]http://www.dneonline.com/calculator.asmx?wsdl\n"
                + "- a reference file called anyname-i-want.url which contains a line with the URL\n"
                + "  (same format as above for OpenAPI or WSDL)."
                + "  If not specified, the API Specification configuration is read directly from the API-Config file.");
        option.setRequired(true);
        option.setArgName("swagger_file.json");
        addOption(option);

        option = new Option("c", "config", true, "This is the JSON-Formatted API-Config containing information how to expose the API. You may get that config file using apim api get with output set to JSON.");
        option.setRequired(true);
        option.setArgName("api_config.json");
        addOption(option);

        option = new Option("backendAuthType", true, "Backend API Authentication Type - Supported type - httpbasic, httpdigest, apikey, oauth and mutualssl");
        option.setArgName("httpbasic");
        addOption(option);

        option = new Option("frontendAuthType", true, "Frontend API Authentication Type - Supported type - apikey, httpbasic, oauth, oauthext, passthrough, aws-sign-header, aws-sign-query and mutualssl");
        option.setArgName("oauth");
        addOption(option);


    }

    @Override
    public void printUsage(String message, String[] args) {
        super.printUsage(message, args);
        Console.println("----------------------------------------------------------------------------------------");
        Console.println("How to Generate Config files");
        Console.println("Generate API manager configuration file based on Open API Specification");
        Console.println(getBinaryName() + " template generate -c samples/config-api-specification.json -a samples/openapi.json");
        Console.println("For more information and advanced examples please visit:");
        Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
    }

    @Override
    protected String getAppName() {
        return "Application-Export";
    }

    @Override
    public Parameters getParams() {
        GenerateTemplateParameters params = new GenerateTemplateParameters();
        params.setApiDefinition(getValue("apidefinition"));
        params.setConfig(getValue("config"));
        String backendAuthType = getValue("backendAuthType");
        if (backendAuthType == null) {
            backendAuthType = "none";
        }
        params.setBackendAuthType(backendAuthType);

        String frontendAuthType = getValue("frontendAuthType");
        if (frontendAuthType == null) {
            frontendAuthType = "passThrough";
        }
        params.setFrontendAuthType(frontendAuthType);
        return params;
    }
}
