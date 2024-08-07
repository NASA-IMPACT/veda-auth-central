# veda-auth-central

Veda Auth Central is a centralized authentication and authorization service designed to manage identity and access control across a suite of VEDA applications. Leveraging Keycloak for authentication and a custom layer for enhanced authorization, Veda Auth Central provides a robust, scalable, and secure solution for managing user roles, permissions, and access across multiple environments.

## Features
- **Centralized Authentication**: Utilizes Keycloak integrated with CILogon for federated authentication across multiple identity providers.
- **Fine-Grained Authorization**: Centralized management of fine-grained authorization, with support for custom scopes, roles, and policies across all VEDA applications.
- **Application Catalog**: Enables administrators to easily register new applications and manage application-specific entities and templates for consistent access control.
- **User Enrichment and Onboarding**: Streamlines user onboarding with both manual and automated group assignments based on user attributes and IDP metadata.
- **Environment-Specific Management**: Allows for the configuration of redirect URLs and access controls tailored to development, staging, and production environments.


## Architecture

Veda Auth Central is built on a Apache Airavata Custos, with Keycloak serving as the core identity provider. A custom layer is implemented on top of Keycloak to manage complex authorization scenarios, application-specific configurations, and user onboarding processes.
