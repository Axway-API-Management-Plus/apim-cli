package com.axway.apim.apiimport.actions;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIMethodAdapter;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageApiMethods {

    private static final Logger LOG = LoggerFactory.getLogger(ManageApiMethods.class);

    public void updateApiMethods(String frontendApiId, List<APIMethod> actualApiMethods, List<APIMethod> desiredApiMethods) throws AppException {
        APIManagerAdapter apiManager = APIManagerAdapter.getInstance();
        if (desiredApiMethods != null) {
            // Ignore if actual and desired methods are same
            List<APIMethod> differences = actualApiMethods.stream()
                    .filter(desiredApiMethods::contains)
                    .collect(Collectors.toList());
            desiredApiMethods.removeAll(differences);
            LOG.info("Total number of methods to be updated : {}", desiredApiMethods.size());
            if (desiredApiMethods.size() > 0) {
                APIManagerAPIMethodAdapter apiManagerAPIMethodAdapter = apiManager.methodAdapter;
                List<APIMethod> apiMethods = apiManagerAPIMethodAdapter.getAllMethodsForAPI(frontendApiId);
                List<String> updatedMethodNames = new ArrayList<>();
                for (APIMethod apiMethod : desiredApiMethods) {
                    for (APIMethod method : apiMethods) {
                        String operationName = method.getName();
                        if (operationName.equals(apiMethod.getName())) {
                            apiMethod.setId(method.getId());
                            apiMethod.setApiId(method.getApiId());
                            apiMethod.setVirtualizedApiId(method.getVirtualizedApiId());
                            apiMethod.setApiMethodId(method.getApiMethodId());
                            apiManagerAPIMethodAdapter.updateApiMethod(apiMethod);
                            updatedMethodNames.add(operationName);
                            break;
                        }
                    }
                }
                for (APIMethod apiMethod : desiredApiMethods) {
                    if (!updatedMethodNames.contains(apiMethod.getName())) {
                        LOG.warn("API Method {} in config file is not matching with API Manager, So ignoring it", apiMethod.getName());
                    }
                }
            }
        }
    }

    public void isMethodMismatch(List<APIMethod> actualApiMethods, List<APIMethod> desiredApiMethods) throws AppException {
        if (desiredApiMethods == null)
            return;

        if (desiredApiMethods.size() > actualApiMethods.size()) {
            LOG.error("API Methods mismatch - Number of API Methods in API Manager : {} and Number of API Methods in API config file : {}", actualApiMethods.size(), desiredApiMethods.size());
            throw new AppException("API Methods mismatch", ErrorCode.BREAKING_CHANGE_DETECTED);
        }
        List<String> desiredApiMethodsName = desiredApiMethods.stream().map(APIMethod::getName).collect(Collectors.toList());
        List<String> actualApiMethodsName = actualApiMethods.stream().map(APIMethod::getName).collect(Collectors.toList());
        for (String desiredApiMethodName : desiredApiMethodsName) {
            if (!actualApiMethodsName.contains(desiredApiMethodName)) {
                LOG.error("API Method mismatch - Name of API Method  - {} - in API config file is not matching with  name of API Method in API Manager", desiredApiMethodName);
                throw new AppException("API Method mismatch", ErrorCode.BREAKING_CHANGE_DETECTED);
            }
        }
    }
}
