# Adding a Custom Layer on Top of Keycloak for Veda Auth Central
* Status: In Review
* Authors: @smarru, @DImuthuUpe, @lahirujayathilake
* Deciders: @freitagb, @alukach, @wildintellect, @yuvipanda, @j08lue, @smohiudd, @slesaad, 
* As of: 2024/08


## Context and Problem Statement

Veda Auth Central is designed to provide a centralized authentication and authorization service for a suite of VEDA applications. While Keycloak is being used as the core identity provider (IDP) interface, several key requirements have emerged that necessitate a custom layer on top of Keycloak. These requirements include fine-grained control over authorization policies, the ability to manage application-specific access controls, and the need to handle complex user onboarding and group management processes, which are not fully supported by Keycloak’s native capabilities.

Within the open-source community, Keycloak is a well-established solution for authentication, while in the commercial sector, Auth0 is a popular choice. Both solutions offer standards-based authentication, leveraging widely adopted protocols like OAuth 2.0 and OpenID Connect. While OAuth is a standard for authorization, there is no standardized way to enforce authorization policies across different applications. This lack of standardization often leads to ad-hoc approaches, where each application develops its own method for managing access controls. Although Keycloak offers advanced capabilities for authorization through scopes, the process of injecting and managing these scopes can be complex and often requires JavaScript injection, making it less ideal for straightforward integration. This highlights the need for a centralized mechanism to administer authorization policies effectively, ensuring consistency and security across the board.

For VEDA, we are leveraging the OAuth2.0 specification with Keycloak as the implementation for authentication. We are also standardizing and centralizing authorization across all VEDA applications, while enabling application-specific administration for enhanced fine-grained security.

## Decision
VEDA Auth Central deploys and manages out of the box open source Keycloack for VEDA Authentication Needs following DevSecOps best practices. In addition a lightweight, pass-through custom layer will be implemented on top of Keycloak in Veda Auth Central to address the following needs:

1. **Authentication with Keycloak**:
   - Veda Auth Central utilizes Keycloak as the primary Identity Provider (IDP) interface, integrated with CILogon to support federated authentication. The Veda Auth Central backend proxies authentication requests to Keycloak, enabling centralized management and secure handling of user credentials across the VEDA ecosystem.

2. **Proxying Authentication Requests**:
   - The authentication process is proxied through Veda Auth Central to capture and enrich the access token issued by Keycloak. This allows the system to extract IDP-specific metadata and append VEDA-specific authorization scopes to the final token, ensuring that the token returned to the user is customized and fully aligned with the application's security requirements.

3. **Application Catalog and Identity Integration**:
   - In addition to managing the general authorization flow, the Veda Auth Central backend provides an advanced application catalog feature. This catalog allows service administrators to easily register VEDA applications and quickly integrate them with the identity management system, streamlining the process of identity and access management for new and existing applications.
   - **Application-Specific Entities**: The application catalog introduces the concept of an application entity within Auth Central, enabling each VEDA app deployment to be managed as a distinct security entity. This approach allows administrators to tailor service-level access policies and user roles to the specific needs of each application, minimizing the risk of misconfiguration or policy overlap across different applications.
   - **Reusable Templates**: To further simplify the management process, administrators can create and manage templates for common application types (e.g., Grafana, STAC, Hub). These templates come with predefined scopes and role requirements, which can be easily applied when registering new applications, ensuring consistent security configurations and reducing setup time.

4. **User Enrichment and Onboarding**:
   - Veda Auth Central includes a comprehensive user enrichment module that centralizes the onboarding process for new users across all applications. This module supports both manual and automated group assignments, ensuring that users are appropriately categorized and granted the correct access permissions from the start.
   - **User Enrichment Module**: The custom layer within Veda Auth Central is responsible for handling user onboarding, offering a streamlined approach that includes both manual and automatic group assignments. These assignments are based on user attributes, such as organizational affiliation, or identity provider (IDP) metadata. This feature is essential for maintaining a clear and organized structure of user roles across multiple VEDA applications, ensuring consistency and security in access control.
   - **Automated Group Assignments**: Unlike Keycloak, which does not have built-in support for automatic group assignments, Veda Auth Central’s custom layer introduces a visual policy enforcement interface. This interface allows administrators to automate group membership and role assignments based on user characteristics, significantly improving efficiency and reducing administrative overhead. By automating this process, Veda Auth Central ensures that users are seamlessly integrated into the appropriate groups and roles, enhancing the overall user management experience.

5. **Fine-Grained Authorization Management:**
   - **Centralized Control**: The custom layer will provide centralized management of fine-grained authorization, allowing for specific scopes and roles to be defined and enforced for each VEDA application. This includes the ability to manage service-specific access policies such as `grafana:user`, `grafana:admin`, `stac:editor`, `stac:reader`, `hub:vms:profile1`, and `hub:vms:profile2`.
   - **Complex Authorization Scenarios**: The custom layer will support advanced authorization scenarios, such as scope inheritance between applications. For instance, the VEDA Hub may need to inherit scopes from a STAC application, enabling seamless data access across related services.


## Drivers
- **Supports Complex Authorization Needs**: Support application-based user subscriptions, scope inheritance, and detailed access control for multiple environments (dev, staging, production).
- **Simple Administration**: How easy it is for administrators such as science program leads to manage users or groups on their own
- **Graphical User Interface**: Ease of providing a graphical user interface for user and group administration
- **Community friendly**: How well the solution fits into VEDA as an ecosystem of community-maintained open source platform components
- **Generic integration**: Whether the solution offers generic integration patterns, such that applications can reuse logic for other publicly available providers like Auth0
- **Well documented**: Quality and completeness of documentation, important to ensure good uptake by applications and other instances of the platform

## Considered Options:

1. **Use Vanilla Keycloak Functionality**
   - **Pros:**
     - Simple to implement with minimal development effort.
     - Utilizes Keycloak’s built-in features and administration tools.
     - Lower initial setup cost.
     - Comes with an admin GUI.
   - **Cons:**
     - Lacks support for application-specific authorization, complex user onboarding, and automated group management.
     - Cannot support advanced use cases like scope inheritance or application-based user subscriptions.

2. **Implement Custom Logic Directly in Keycloak**
   - **Pros:**
     - Keeps all logic within Keycloak, potentially simplifying the architecture.
     - Leverages Keycloak’s infrastructure for execution and management.
   - **Cons:**
     - Requires deep customization of Keycloak, which could complicate future upgrades and maintenance.
     - Does not fully address the need for a centralized and application-specific authorization layer.
     - Increases development time and complexity within Keycloak, potentially leading to higher long-term costs.

3. **Add a Custom Layer on Top of Keycloak** (Chosen Option)
   - **Pros:**
     - Provides the flexibility to implement specific business logic, including fine-grained authorization and user management.
     - Allows Keycloak to be upgraded and maintained independently of the custom logic.
     - Supports the scalability and customization needs of Veda Auth Central without overly complicating the core IAM solution.
     - By introducing application-specific entities and reusable templates, the custom layer simplifies the management of access policies and reduces the risk of misconfiguration that could arise from directly managing these controls within Keycloak.
   - **Cons:**
     - Additional effort required to develop and maintain the custom layer and its documentation.
     - Potential for increased complexity in managing the interaction between Keycloak and the custom layer.
     - Risk of requiring solution-specific logic on the application side.
     - Requires development of a custom administation GUI.

## Decision Outcome

**Architectural Description**

1. [VEDA Authentication Flows](../architecture/1-veda-authn-flow.md)
2. [VEDA Authorization FLows](../architecture/2-veda-authz-flow.md)
3. [VEDA Auth Central App-admin FLows](../architecture/3-vac-app-admin.md)


## Conclusion
The decision to add a custom layer on top of Keycloak for Veda Auth Central is driven by the need for enhanced flexibility, control, and the ability to meet specific authorization and user management requirements that are critical for the VEDA applications. This approach balances the use of a robust, proven IAM solution (Keycloak) with the need to address specific architectural and operational requirements, ensuring that Veda Auth Central can provide a tailored and effective authentication and authorization service.

