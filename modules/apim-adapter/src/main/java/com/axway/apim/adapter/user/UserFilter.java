package com.axway.apim.adapter.user;

import com.axway.apim.adapter.apis.FilterHelper;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CustomPropertiesFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserFilter implements CustomPropertiesFilter {

    public static final String FIELD = "field";
    public static final String VALUE = "value";
    public static final String OP = "op";
    private String id;
    String description;
    String email;
    boolean enabled;
    String name;
    String loginName;
    String phone;
    String role;
    String type;

    String organizationName;

    boolean includeImage;

    private List<String> customProperties;

    private final List<NameValuePair> filters = new ArrayList<>();

    private UserFilter() {
    }

    public void setDescription(String description) {
        if (description == null) return;
        this.description = description;
        filters.add(new BasicNameValuePair(FIELD, "description"));
        filters.add(new BasicNameValuePair(OP, "like"));
        filters.add(new BasicNameValuePair(VALUE, description));
    }

    public void setEmail(String email) {
        if (email == null) return;
        if (email.equals("*")) return;
        this.email = email;
        String op = "eq";
        if (email.startsWith("*") || email.endsWith("*")) {
            op = "like";
            email = email.replace("*", "");
        }
        filters.add(new BasicNameValuePair(FIELD, "email"));
        filters.add(new BasicNameValuePair(OP, op));
        filters.add(new BasicNameValuePair(VALUE, email.toLowerCase()));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        filters.add(new BasicNameValuePair(FIELD, "enabled"));
        filters.add(new BasicNameValuePair(OP, "eq"));
        filters.add(new BasicNameValuePair(VALUE, (enabled) ? "enabled" : "disabled"));
    }

    public void setName(String name) {
        if (name == null) return;
        if (name.equals("*")) return;
        this.name = name;
        FilterHelper.setFilter(name, filters);
    }

    public void setLoginName(String loginName) {
        if (loginName == null) return;
        if (loginName.equals("*")) return;
        this.loginName = loginName;
        String op = "eq";
        if (loginName.startsWith("*") || loginName.endsWith("*")) {
            op = "like";
            loginName = loginName.replace("*", "");
        }
        filters.add(new BasicNameValuePair(FIELD, "loginName"));
        filters.add(new BasicNameValuePair(OP, op));
        filters.add(new BasicNameValuePair(VALUE, loginName));
    }

    public void setPhone(String phone) {
        if (phone == null) return;
        this.phone = phone;
        filters.add(new BasicNameValuePair(FIELD, "phone"));
        filters.add(new BasicNameValuePair(OP, "eq"));
        filters.add(new BasicNameValuePair(VALUE, phone));
    }

    public void setRole(String role) {
        if (role == null) return;
        this.role = role;
        filters.add(new BasicNameValuePair(FIELD, "role"));
        filters.add(new BasicNameValuePair(OP, "eq"));
        filters.add(new BasicNameValuePair(VALUE, role));
    }

    public void setType(String type) {
        if (type == null) return;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public List<NameValuePair> getFilters() {
        return filters;
    }

    public String getName() {
        return name;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isIncludeImage() {
        return includeImage;
    }

    public List<String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(List<String> customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof UserFilter)) return false;
        UserFilter other = (UserFilter) obj;
        return (
            StringUtils.equals(other.getId(), this.getId()) &&
                StringUtils.equals(other.getLoginName(), this.getLoginName()) &&
                other.isEnabled() == this.isEnabled()
        );
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += (this.id != null) ? this.id.hashCode() : 0;
        hashCode += (this.loginName != null) ? this.loginName.hashCode() : 0;
        return hashCode;
    }

    @Override
    public String toString() {
        return "UserFilter [loginName=" + loginName + ", id=" + id + "]";
    }

    public boolean filter(User user) {
        if (this.getType() == null && this.getOrganizationName() == null) { // Nothing given to filter out.
            return true;
        }
        if (this.getType() != null && !this.getType().equals(user.getType())) {
            return false;
        }
        if (this.getOrganizationName() != null) {
            Pattern pattern = Pattern.compile(this.getOrganizationName().replace("*", ".*"));
            Matcher matcher = pattern.matcher(user.getOrganization().getName());
            return matcher.matches();
        }
        return true;
    }


    /**
     * Build an applicationAdapter based on the given configuration
     */
    public static class Builder {

        String id;
        String description;
        String email;
        boolean enabled;
        boolean useEnabledFilter = false;
        String name;
        String loginName;
        String phone;
        String role;
        String type;
        String organizationName;

        boolean includeImage;

        private List<String> customProperties;


        public Builder() {
            super();
        }

        public UserFilter build() {
            UserFilter filter = new UserFilter();
            filter.setId(this.id);
            filter.setDescription(this.description);
            filter.setEmail(this.email);
            if (useEnabledFilter) filter.setEnabled(this.enabled);
            filter.setName(this.name);
            filter.setLoginName(this.loginName);
            filter.setPhone(this.phone);
            filter.setRole(this.role);
            filter.setType(this.type);
            filter.setOrganizationName(this.organizationName);
            filter.includeImage = this.includeImage;
            filter.setCustomProperties(this.customProperties);
            return filter;
        }

        public Builder hasId(String id) {
            this.id = id;
            return this;
        }

        public Builder hasDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder hasEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder hasName(String name) {
            this.name = name;
            return this;
        }

        public Builder hasType(String type) {
            this.type = type;
            return this;
        }

        public Builder hasLoginName(String loginName) {
            this.loginName = loginName;
            return this;
        }

        public Builder hasRole(String role) {
            this.role = role;
            return this;
        }

        public Builder hasOrganization(String organizationName) {
            this.organizationName = organizationName;
            return this;
        }

        public Builder isEnabled(Boolean enabled) {
            if (enabled == null) return this;
            this.useEnabledFilter = true;
            this.enabled = enabled;
            return this;
        }

        public Builder includeImage(boolean includeImage) {
            this.includeImage = includeImage;
            return this;
        }

        public Builder includeCustomProperties(List<String> customProperties) {
            this.customProperties = customProperties;
            return this;
        }
    }
}
