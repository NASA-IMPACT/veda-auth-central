# veda-auth-central

Veda Auth Central is a centralized authentication and authorization service designed to manage identity and access control across a suite of VEDA applications. 

## Goal: Seamless and Secure User Experience for VEDA Applications

The goal of VEDA Auth Central is to ensure that users have a seamless and secure experience in both authentication and authorization when using VEDA applications. Auth Central functions as an OAuth2 service and a credential store, streamlining the login process and safeguarding user credentials. When users attempt to access any VEDA application, they are redirected to the Auth Central login page, powered by CILogon, where they can authenticate using institutional credentials or a federated identity provider such as Microsoft, Google, or GitHub. Once authenticated within the OAuth2 session, Auth Central processes the request to determine the appropriate authorization scopes based on the user’s roles and access needs. Auth Central then enforces authorization policies to ensure precise scope assignment. After deriving these scopes, Auth Central provides an access token, enabling secure access to the requested VEDA applications (such as Grafana, Hub, STAC API, or VEDA Open Science Gateway) following the authorization code grant flow. The goal is to deliver a user-friendly, efficient, and secure access experience across the VEDA ecosystem.

## Implementation Detail

VEDA Auth Central integrates best-of-breed open-source projects to provide a comprehensive and secure authentication and authorization service for VEDA applications. The system leverages Keycloak for its robust identity management capabilities, enabling standards-based authentication using protocols like OAuth2.0 and OpenID Connect. Keycloak serves as the primary Identity Provider (IDP), integrated with CILogon to support federated authentication.

To manage sensitive credentials, VEDA Auth Central utilizes HashiCorp Vault, a leading solution for secrets management. HashiCorp Vault securely stores and manages secrets, such as API keys, SSH credentials, and passwords, providing a secure vault for VEDA applications to store and retrieve sensitive information. This integration ensures that all secrets are encrypted and access is tightly controlled, enhancing the overall security posture of the VEDA ecosystem.

# Apache Airavata Custos Leverage 
The integration framework for VEDA Auth Central is built upon the Apache Airavata Custos security framework. Custos has effectively addressed security requirements similar to those of VEDA for various science gateways, including the Science Gateways Platform as a Service, HathiTrust Research Center, and the Galaxy Project. By building on Custos, VEDA Auth Central minimizes the burden of maintaining complex Keycloak integrations, ensuring a robust and scalable security solution.

## Features
- **Centralized Authentication**: Utilizes Keycloak integrated with CILogon for federated authentication across multiple identity providers.
- **Fine-Grained Authorization**: Centralized management of fine-grained authorization, with support for custom scopes, roles, and policies across all VEDA applications.
- **Application Catalog**: Enables administrators to easily register new applications and manage application-specific entities and templates for consistent access control.
- **User Enrichment and Onboarding**: Streamlines user onboarding with both manual and automated group assignments based on user attributes and IDP metadata.
- **Environment-Specific Management**: Allows for the configuration of redirect URLs and access controls tailored to development, staging, and production environments.
