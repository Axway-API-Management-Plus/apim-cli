package com.axway.apim.user.adapter.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.users.impl.UserResultHandler;
import com.axway.apim.users.lib.cli.UserExportCLIOptions;
import com.axway.apim.users.lib.params.UserExportParams;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class ConsoleUserExporterTest extends WiremockWrapper {

    private APIManagerAdapter apimanagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apimanagerAdapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void testConsoleExport() throws AppException {
        String[] args = {"-loginName", "usera"};
        UserExportParams params = (UserExportParams) UserExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        UserResultHandler exporter = UserResultHandler.create(UserResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<User> users = apimanagerAdapter.userAdapter.getUsers(exporter.getFilter());
        exporter.export(users);
    }

    @Test
    public void testConsoleExportWide() throws AppException {
        String[] args = {"-loginName", "usera", "-wide"};
        UserExportParams params = (UserExportParams) UserExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        UserResultHandler exporter = UserResultHandler.create(UserResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<User> users = apimanagerAdapter.userAdapter.getUsers(exporter.getFilter());
        exporter.export(users);
    }

    @Test
    public void testConsoleExportUltra() throws AppException {
        String[] args = {"-loginName", "usera", "-ultra"};
        UserExportParams params = (UserExportParams) UserExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        UserResultHandler exporter = UserResultHandler.create(UserResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<User> users = apimanagerAdapter.userAdapter.getUsers(exporter.getFilter());
        exporter.export(users);
    }
}
