package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DATAPIExporter extends APIResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DATAPIExporter.class);

    /**
     * Where to store the exported API-Definition
     */
    private final String givenExportFolder;

    private final String datPassword;

    APIManagerAdapter apiManager = APIManagerAdapter.getInstance();

    public DATAPIExporter(APIExportParams params) throws AppException {
        super(params);
        this.givenExportFolder = params.getTarget();
        this.datPassword = params.getDatPassword();
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        for (API api : apis) {
            saveAPILocally(api);
        }
    }

    @Override
    public APIFilter getFilter() {
        Builder builder = getBaseAPIFilterBuilder();
        return builder.build();
    }

    public void saveAPILocally(API api) throws AppException {
        String apiPath = getAPIExportFolder(api.getPath());
        String vhost = getVHost(api).replace(":", "_");
        File localFolder = new File(this.givenExportFolder + File.separator + vhost + File.separator + apiPath);
        LOG.debug("Going to export API: {} into folder: {}", api, localFolder);
        validateFolder(localFolder);
        byte[] datFileContent = apiManager.getApiAdapter().getAPIDatFile(api, datPassword);
        String targetFile = null;
        try {
            targetFile = localFolder.getCanonicalPath() + "/" + api.getName() + ".dat";
            writeBytesToFile(datFileContent, targetFile);
        } catch (IOException e) {
            throw new AppException("Can't save API-DAT file locally: " + targetFile,
                    ErrorCode.UNXPECTED_ERROR, e);
        }
        LOG.info("Successfully exported API: {} as DAT-File into folder: {}", api.getName(), localFolder.getAbsolutePath());
    }

    private String getVHost(API api) throws AppException {
        if (api.getVhost() != null) return api.getVhost();
        String orgVHost = APIManagerAdapter.getInstance().getOrgAdapter().getOrg(new OrgFilter.Builder().hasId(api.getOrganizationId()).build()).getVirtualHost();
        if (orgVHost != null) return orgVHost;
        return "";
    }
}
