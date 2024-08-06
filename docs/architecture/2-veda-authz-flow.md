## **Authorization in Veda Auth Central**

### Overview
Veda Auth Central provides a fine-grained authorization mechanism that is centralized for all VEDA applications. This ensures consistent enforcement of access control policies across the ecosystem.

### Key Features
- **Centralized Authorization**: Authorization decisions are centralized within Veda Auth Central, allowing for uniform policy enforcement across all applications.
- **Role-Based Access Control (RBAC)**: Supports RBAC, enabling administrators to define roles and associate them with specific permissions.
- **Custom Scopes and Inheritance**: Allows for the definition of custom authorization scopes, which can be inherited across different applications.

### Architecture
- **Authorization Flow**:
  1. Upon receiving a user request, the application queries Veda Auth Central for authorization.
  2. Veda Auth Central checks the user's groups and assigns application specific roles and corresponding scopes associated with their access token.
  3. Based on predefined policies, Veda Auth Central grants or denies access to the requested resources.

- **Group and Role Management**:
  - Veda Auth Central manages user roles and groups centrally, automating the assignment based on user attributes or identity provider metadata. This ensures that users have the appropriate access across all VEDA applications.
  
- **Policy Management**:
  - Administrators can define and manage authorization policies through a centralized interface. These policies are applied consistently across all applications, reducing the risk of misconfiguration.

#### Integration
- Applications interact with Veda Auth Central via APIs to enforce authorization decisions. The use of standardized protocols like OAuth 2.0 ensures compatibility with other systems.
- Custom authorization logic can be added to Veda Auth Central to meet specific application needs, providing flexibility while maintaining centralized control.