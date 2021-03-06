/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.sts.token.validator;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.cxf.interceptor.security.DefaultSecurityContext;
import org.apache.cxf.interceptor.security.RolePrefixSecurityContextImpl;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.rt.security.saml.SAMLSecurityContext;
import org.apache.cxf.rt.security.saml.SAMLUtils;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;

/**
 * A default implementation to extract roles from a SAML Assertion
 */
public class DefaultSAMLRoleParser implements SAMLRoleParser {
    
    /**
     * This configuration tag specifies the default attribute name where the roles are present
     * The default is "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role".
     */
    public static final String SAML_ROLE_ATTRIBUTENAME_DEFAULT =
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role";
    
    private boolean useJaasSubject = true;
    private String roleClassifier;
    private String roleClassifierType = "prefix";
    private String roleAttributeName = SAML_ROLE_ATTRIBUTENAME_DEFAULT;

    /**
     * Return the set of User/Principal roles from the Assertion.
     * @param principal the Principal associated with the Assertion
     * @param subject the JAAS Subject associated with a successful validation of the Assertion
     * @param assertion The Assertion object
     * @return the set of User/Principal roles from the Assertion.
     */
    public Set<Principal> parseRolesFromAssertion(
        Principal principal, Subject subject, SamlAssertionWrapper assertion
    ) {
        if (subject != null && useJaasSubject) {
            if (roleClassifier != null && !"".equals(roleClassifier)) {
                RolePrefixSecurityContextImpl securityContext =
                    new RolePrefixSecurityContextImpl(subject, roleClassifier, roleClassifierType);
                return securityContext.getUserRoles();
            } else {
                return new DefaultSecurityContext(principal, subject).getUserRoles();
            }
        }
        
        ClaimCollection claims = SAMLUtils.getClaims(assertion);
        Set<Principal> roles = 
            SAMLUtils.parseRolesFromClaims(claims, roleAttributeName, null);
        
        SAMLSecurityContext context = 
            new SAMLSecurityContext(principal, roles, claims);
        
        return context.getUserRoles();
    }
    
    public boolean isUseJaasSubject() {
        return useJaasSubject;
    }

    /**
     * Whether to get roles from the JAAS Subject (if not null) returned from SAML Assertion
     * Validation or not. The default is true.
     * @param useJaasSubject whether to get roles from the JAAS Subject or not
     */
    public void setUseJaasSubject(boolean useJaasSubject) {
        this.useJaasSubject = useJaasSubject;
    }

    public String getRoleClassifier() {
        return roleClassifier;
    }

    /**
     * Set the Subject Role Classifier to use. If this value is not specified, then it tries to
     * get roles from the supplied JAAS Subject (if not null) using the DefaultSecurityContext 
     * in cxf-rt-core. Otherwise it uses this value in combination with the 
     * SUBJECT_ROLE_CLASSIFIER_TYPE to get the roles from the Subject.
     * @param roleClassifier the Subject Role Classifier to use
     */
    public void setRoleClassifier(String roleClassifier) {
        this.roleClassifier = roleClassifier;
    }

    public String getRoleClassifierType() {
        return roleClassifierType;
    }

    /**
     * Set the Subject Role Classifier Type to use. Currently accepted values are "prefix" or 
     * "classname". Must be used in conjunction with the SUBJECT_ROLE_CLASSIFIER. The default 
     * value is "prefix".
     * @param roleClassifierType the Subject Role Classifier Type to use
     */
    public void setRoleClassifierType(String roleClassifierType) {
        this.roleClassifierType = roleClassifierType;
    }
    
    
    public String getRoleAttributeName() {
        return roleAttributeName;
    }

    /**
     * Set the attribute URI of the SAML AttributeStatement where the role information is stored.
     * The default is "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role".
     * @param roleAttributeName the Attribute URI where role information is stored
     */
    public void setRoleAttributeName(String roleAttributeName) {
        this.roleAttributeName = roleAttributeName;
    }

}