## **Authentication in Veda Auth Central**

### Overview
Veda Auth Central uses Keycloak as the primary identity provider (IDP) to manage user authentication. It integrates with CILogon to support federated authentication across a wide range of identity providers, including academic institutions and social platforms.

### Key Features
- **Federated Authentication**: Integration with CILogon enables users to authenticate using credentials from over 4,500 identity providers.
- **Single Sign-On (SSO)**: Supports SSO across multiple VEDA applications, ensuring a seamless user experience.
- **OAuth 2.0 Implementation**: Follows OAuth 2.0 standards to provide secure access tokens that encapsulate user identity and permissions.

### Architecture
- **Authentication Flow**:
  1. The user initiates an authentication request via a VEDA application.
  2. Veda Auth Central proxies this request to Keycloak.
  3. Keycloak communicates with CILogon (if federated authentication is needed) or handles the authentication directly.
  4. Upon successful authentication, Keycloak issues an OAuth 2.0 access token, which is enriched by Veda Auth Central with additional metadata (application specific scopes, groups) before being returned to the application.
  
- **User Management**:
  - User profiles are managed within Keycloak realms specific to each VEDA application. Veda Auth Central handles user provisioning and deprovisioning, ensuring that user data is consistent across the ecosystem.

### Integration
- VEDA applications integrate with Veda Auth Central via RESTful APIs or gRPC endpoints, allowing them to authenticate users, validate tokens, and access user information.
- Support for OAuth 2.0 and OpenID Connect ensures interoperability with other systems.