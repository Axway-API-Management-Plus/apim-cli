package com.axway.apim.user.adapter.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.users.impl.UserResultHandler;
import com.axway.apim.users.lib.cli.UserExportCLIOptions;
import com.axway.apim.users.lib.params.UserExportParams;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class JsonUserExporterTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void testJsonExport() throws AppException {
        String[] args = {"-h", "localhost", "-loginName", "usera"};
        UserExportParams params = (UserExportParams) UserExportCLIOptions.create(args).getParams();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        ExportResult result = new ExportResult();
        UserResultHandler exporter = UserResultHandler.create(UserResultHandler.ResultHandler.JSON_EXPORTER, params, result);
        List<User> users = apimanagerAdapter.getUserAdapter().getUsers(exporter.getFilter());
        exporter.export(users);
    }
}
