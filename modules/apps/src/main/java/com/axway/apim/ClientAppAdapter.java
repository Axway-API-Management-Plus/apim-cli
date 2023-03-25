package com.axway.apim;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;

import java.util.List;

public abstract class ClientAppAdapter {

    protected List<ClientApplication> apps;

    protected Result result;

    protected ClientAppAdapter() {

    }

    /**
     * Returns a list of application according to the provided filter
     *
     * @return applications according to the provided filter
     * @throws AppException when something goes wrong
     */
    public List<ClientApplication> getApplications() throws AppException {
        if (this.apps == null) readConfig();
        return this.apps;
    }

    protected abstract void readConfig() throws AppException;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
