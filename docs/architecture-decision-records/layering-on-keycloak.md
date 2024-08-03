# Adding a Custom Layer on Top of Keycloak for Veda Auth Central
* Status: In Review
* Authors: @smarru, @DImuthuUpe, @lahirujayathilake
* Deciders: @freitagb, @alukach, @wildintellect, @yuvipanda, @j08lue, @smohiudd, @slesaad, 
* As of: 2024/08


## Context and Problem Statement

Veda Auth Central is designed to provide a centralized authentication and authorization service for a suite of VEDA applications. While Keycloak offers robust identity and access management features out of the box, the specific authorization needs of Veda Auth Central may not be fully met by the default functionality. Key requirements include fine-grained control over policy enforcement, automated group assignments based on specific business logic, and the integration of additional security and user management features. A custom layer on top of Keycloak is considered to meet these requirements effectively.

## Decision
A custom layer will be implemented on top of Keycloak in Veda Auth Central to address the following needs:
- **Fine Grained Authorization:** Ensure that access controls are consistently enforced across all VEDA applications, and that each application can rely on a centralized system to manage complex authorization rules and policies.
- **Diverse Application Ecosystem:** The VEDA environment comprises multiple applications, many of them have overlapps, but each may have subet of set of users, roles, and access requirements. Fine-grained authorization allows Veda Auth Central to manage permissions at a detailed level, ensuring that each application can enforce the appropriate access controls based on the specific needs of its users.
- **Consistency Across Applications:** Centralizing fine-grained authorization within Veda Auth Central ensures that all VEDA applications follow a uniform approach to access control. This consistency reduces the risk of security gaps that could arise from disparate authorization mechanisms being implemented independently within each application. It also simplifies the management and auditing of access policies across the entire VEDA ecosystem.
- **Scalability and Flexibility:** As VEDA applications evolve and new ones are introduced, the need for scalable and flexible authorization mechanisms becomes more pronounced. Fine-grained authorization in Veda Auth Central allows for dynamic policy management, where new roles, permissions, and access rules can be defined and applied centrally without requiring changes to individual applications. This flexibility supports the rapid deployment of new applications and the adaptation of existing ones to changing security requirements.
- **Enhanced Policy Support:** This layer will provide support for custom policies that are not natively supported by Keycloak. These policies will be crucial for enforcing access restrictions and permissions across VEDA applications.
- **Automated Group Assignment:** The custom layer will automate group assignments based on predefined criteria, which Keycloak does not handle natively. This functionality is necessary for managing complex user roles and privileges dynamically.
- **Additional Logic for Specific Use Cases:** Veda Auth Central requires logic that goes beyond the capabilities of vanilla Keycloak. This custom logic will be implemented in the additional layer to ensure that the system can handle specific business rules and use cases unique to the VEDA environment.

## Drivers
- **Supports Foreseen Use Cases:** The custom layer will be designed to handle specific requirements and use cases that are essential for VEDA applications but are not natively supported by Keycloak.
- **Flexibility in Policy Management:** By adding a custom layer, Veda Auth Central can implement more flexible and granular policy management, enabling better control over access and authorization across various applications.
- **Low Maintenance Overhead:** The custom layer will be designed to integrate seamlessly with Keycloak, minimizing the impact on maintenance and allowing the core Keycloak functionality to be leveraged without unnecessary complexity.
- **Cost Efficiency:** Implementing the custom logic in a separate layer allows Veda Auth Central to avoid the high costs associated with building or customizing a completely new IAM solution from scratch while still meeting the unique needs of the system.

## Considered Options:

1. **Use Vanilla Keycloak Functionality**
   - **Pros:**
     - Simple to implement with minimal development effort.
     - Utilizes Keycloak’s built-in features and administration tools.
     - Lower initial setup cost.
   - **Cons:**
     - Lacks support for specific authorization business logic and policies required by Veda Auth Central.
     - Limited flexibility in handling group assignments and custom policy enforcement.

2. **Implement Custom Logic Directly in Keycloak**
   - **Pros:**
     - Keeps all logic within Keycloak, potentially simplifying the architecture.
     - Leverages Keycloak’s infrastructure for execution and management.
   - **Cons:**
     - Requires deep customization of Keycloak, which could complicate future upgrades and maintenance.
     - Increases development time and complexity within Keycloak, potentially leading to higher long-term costs.

3. **Add a Custom Layer on Top of Keycloak** (Chosen Option)
   - **Pros:**
     - Provides the flexibility to implement specific authorization logic and policy enforcement.
     - Allows Keycloak to be upgraded and maintained independently of the custom logic.
     - Supports the scalability and customization needs of Veda Auth Central without overly complicating the core IAM solution.
   - **Cons:**
     - Additional development effort required to create and maintain the custom layer.
     - Potential for increased complexity in managing the interaction between Keycloak and the custom layer.

## Decision Outcome

**Architectural Diagram of the proposed Layer**


## Conclusion
The decision to add a custom layer on top of Keycloak for Veda Auth Central is driven by the need for enhanced flexibility, control, and the ability to meet specific use cases that are critical for the VEDA applications. This approach balances the use of a robust, proven IAM solution (Keycloak) with the need to address specific architectural and operational requirements, ensuring that Veda Auth Central can provide a tailored and effective authentication and authorization service.

