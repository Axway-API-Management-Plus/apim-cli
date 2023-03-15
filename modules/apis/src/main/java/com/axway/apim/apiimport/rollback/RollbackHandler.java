package com.axway.apim.apiimport.rollback;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RollbackHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RollbackHandler.class);

    private static RollbackHandler instance;

    private final List<RollbackAction> rollbackActions;

    private RollbackHandler() {
        super();
        rollbackActions = new ArrayList<>();
    }

    public static RollbackHandler getInstance() {
        if (instance == null) {
            instance = new RollbackHandler();
        }
        return instance;
    }

    public static synchronized void deleteInstance() {
        RollbackHandler.instance = null;
    }

    public void addRollbackAction(RollbackAction action) {
        rollbackActions.add(action);
    }

    public void executeRollback() {
        if (!CoreParameters.getInstance().isRollback()) {
            LOG.info("Rollback is disabled.");
            return;
        }
        if (rollbackActions.isEmpty()) return; // Nothing to roll back
        rollbackActions.sort((first, second) -> {
            if (first.getExecuteOrder() > second.getExecuteOrder()) {
                return 1;
            } else {
                return -1;
            }
        });
        for (RollbackAction action : rollbackActions) {
            try {
                action.rollback();
            } catch (AppException e) {
                LOG.error("Can't rollback ", e);
            }
        }
        LOG.info("Rolled back: {}", rollbackActions);
    }
}
