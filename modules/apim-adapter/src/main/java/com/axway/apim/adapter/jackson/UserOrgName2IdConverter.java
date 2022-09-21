package com.axway.apim.adapter.jackson;

import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.User;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.HashMap;
import java.util.Map;

public class UserOrgName2IdConverter extends StdConverter<User, User> {
    @Override
    public User convert(User user) {
        Map<String, String>  orgs2Role =  user.getOrgs2Role();
        Map<String, String>  orgs2Name =  user.getOrgs2Name();
     //   Organization organization = user.getOrganization();
        Map<String, String> orgName2Id = new HashMap<>();
//        if(user.getRole().equals("oadmin")) {
//            orgName2Id.put(organization.getName(), organization.getId());
//        }
        if(orgs2Role != null) {
            for (Map.Entry<String, String> entry : orgs2Role.entrySet()) {
                String orgId = entry.getKey();
                String role = entry.getValue();
                if (role.equals("oadmin") || role.equals("admin")) {
                    orgName2Id.put(orgs2Name.get(orgId), orgId);
                }
            }
            user.setName2OrgId(orgName2Id);
        }
        return user;
    }
}
