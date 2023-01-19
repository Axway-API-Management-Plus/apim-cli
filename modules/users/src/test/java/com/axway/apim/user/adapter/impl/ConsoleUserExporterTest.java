package com.axway.apim.user.adapter.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import com.axway.apim.users.impl.UserResultHandler;
import com.axway.apim.users.lib.cli.UserExportCLIOptions;
import com.axway.apim.users.lib.params.UserExportParams;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class ConsoleUserExporterTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void testConsoleExport() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        String[] args = {"-h", "localhost", "-loginName", "usera"};
        UserExportParams params = (UserExportParams) UserExportCLIOptions.create(args).getParams();
        APIManagerAdapter.deleteInstance();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        ExportResult result = new ExportResult();
        UserResultHandler exporter = UserResultHandler.create(UserResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<User> users = apimanagerAdapter.userAdapter.getUsers(exporter.getFilter());
        exporter.export(users);
    }
}
