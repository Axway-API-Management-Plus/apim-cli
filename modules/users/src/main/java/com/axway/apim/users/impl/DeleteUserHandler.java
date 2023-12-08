package com.axway.apim.users.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.adapter.user.UserFilter.Builder;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.users.lib.params.UserExportParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteUserHandler extends UserResultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUserHandler.class);


    public DeleteUserHandler(UserExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(List<User> users) throws AppException {
        Console.println(users.size() + " selected for deletion.");
        if (CoreParameters.getInstance().isForce()) {
            Console.println("Force flag given to delete: " + users.size() + " User(s)");
        } else {
            if (Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
                Console.println("Okay, going to delete: " + users.size() + " Users(s)");
            } else {
                Console.println("Canceled.");
                return;
            }
        }
        for (User user : users) {
            try {
                APIManagerAdapter.getInstance().getUserAdapter().deleteUser(user);
            } catch (Exception e) {
                LOG.error("Error deleting user: {}", user.getName());
            }
        }
        Console.println("Done!");
    }

    @Override
    public UserFilter getFilter() {
        Builder builder = getBaseFilterBuilder();
        return builder.build();
    }
}
