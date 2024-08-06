# Adding a Custom Layer on Top of Keycloak for Veda Auth Central
* Status: In Review
* Authors: @smarru, @DImuthuUpe, @lahirujayathilake
* Deciders: @freitagb, @alukach, @wildintellect, @yuvipanda, @j08lue, @smohiudd, @slesaad, 
* As of: 2024/08


## Context and Problem Statement

Veda Auth Central is designed to provide a centralized authentication and authorization service for a suite of VEDA applications. While Keycloak is being used as the core identity provider (IDP) interface, several key requirements have emerged that necessitate a custom layer on top of Keycloak. These requirements include fine-grained control over authorization policies, the ability to manage application-specific access controls, and the need to handle complex user onboarding and group management processes, which are not fully supported by Keycloak’s native capabilities.

## Decision
A custom layer will be implemented on top of Keycloak in Veda Auth Central to address the following needs:

1. **Fine-Grained Authorization Management:**
   - **Centralized Control**: The custom layer will provide centralized management of fine-grained authorization, allowing for specific scopes and roles to be defined and enforced for each VEDA application. This includes the ability to manage service-specific access policies such as `grafana:user`, `grafana:admin`, `stac:editor`, and `stac:reader`.
   - **Complex Authorization Scenarios**: The custom layer will support advanced authorization scenarios, such as scope inheritance between applications. For instance, the VEDA Hub may need to inherit scopes from a STAC application, enabling seamless data access across related services.

2. **Application Catalog and Management:**
   - **Application-Specific Entities**: The custom layer introduces the concept of an application entity within Auth Central, allowing each VEDA app deployment to be managed as a distinct security entity. This approach ensures that administrators can manage service-level access policies and user roles specific to each application, without the risk of misconfiguration or overlap.
   - **Reusable Templates**: Admins can create and manage templates for common application types (e.g., Grafana, STAC, Hub) with predefined scope and role requirements, streamlining the process of registering new applications.

3. **Enhanced User Onboarding and Group Management:**
   - **User Enrichment Module**: The custom layer will handle user onboarding, providing both manual and automatic group assignments based on user attributes or identity provider (IDP) metadata. This feature is crucial for maintaining a clear and organized structure of user roles across multiple applications.
   - **Automated Group Assignments**: Unlike Keycloak, which lacks built-in support for automatic group assignments, the custom layer will include a visual policy enforcement interface to automate this process based on user characteristics, improving efficiency and reducing administrative overhead.

4. **Proxying Authentication for Token Enrichment:**
   - **Access Token Management**: The custom layer proxies authentication requests to Keycloak to handle and enrich access tokens with VEDA-specific authorization scopes and IDP-specific metadata. This functionality is essential for ensuring that the final tokens returned to users are fully compliant with VEDA’s security and authorization requirements.

## Drivers
- **Supports Complex Authorization Needs**: The custom layer addresses specific authorization needs that are not natively supported by Keycloak, such as application-based user subscriptions, scope inheritance, and detailed access control for multiple environments (dev, staging, production).
- **Simplifies Administration and Reduces Misconfiguration Risk**: By introducing application-specific entities and reusable templates, the custom layer simplifies the management of access policies and reduces the risk of misconfiguration that could arise from directly managing these controls within Keycloak.
- **Improves User Management**: The user enrichment module centralizes and automates user onboarding and group management, providing a clearer and more organized structure for user roles and permissions across all VEDA applications.

## Considered Options:

1. **Use Vanilla Keycloak Functionality**
   - **Pros:**
     - Simple to implement with minimal development effort.
     - Utilizes Keycloak’s built-in features and administration tools.
     - Lower initial setup cost.
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
   - **Cons:**
     - Additional development effort required to create and maintain the custom layer.
     - Potential for increased complexity in managing the interaction between Keycloak and the custom layer.

## Decision Outcome

**Architectural Diagram of the proposed Layer**


## Conclusion
The decision to add a custom layer on top of Keycloak for Veda Auth Central is driven by the need for enhanced flexibility, control, and the ability to meet specific authorization and user management requirements that are critical for the VEDA applications. This approach balances the use of a robust, proven IAM solution (Keycloak) with the need to address specific architectural and operational requirements, ensuring that Veda Auth Central can provide a tailored and effective authentication and authorization service.

