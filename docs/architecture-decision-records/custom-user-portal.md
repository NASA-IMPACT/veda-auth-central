# A Customs User Portal for VEDA Auth Central
* Status: Working in Progress
* Authors: @eroma2014, @smarru, @DImuthuUpe, @lahirujayathilake
* Deciders: @freitagb, @alukach, @wildintellect, @yuvipanda, @j08lue, @smohiudd, @slesaad
* As of: 2024/08/27


## Context & Problem Statement
NASA VEDA has multiple applications with each one having its own authentication implementation with isolated authorization rules. With a requirement from VEDA leadership to centrally cater all the authentication and authorization needs with added features, it was decided to go for a central authentication and authorization service. To address this a central authentication and authorization system (VEDA Auth Central) was designed to provide a centralized authentication and authorization service for all VEDA applications. This application is to meet specific requirements, including fine grained control over authroization policies, application-specific access control management, and handling comple user onboarding and group management processes. The next priority was to empower the VEDA application owners, and end-users to easily utilize the VEDA Auth services thrhough straightforward configurations, without coding, without complex steps. This focus then expanded to have a web-based portal for managing these configurations.


## Decision
For VEDA Auth Central portal, it is essential to provide a lightweight, user-friendly interface that caters to a diversse range of users, including VEDA leadership, application owners and developers, and application end-users. A single portal that serves all these user categories should be intutive, easy to configre, and maintain, ensuring it remains an effective service. The portal should require efficient developement and configuration effort, allowing for effective implementation while maintain its lightweight nature. Additionally, the system should be easy to learn, requiring minimal to no instructions or training so that users can quickly and seamleslly integrate it into their workflows. 


## Drivers
**Easy to use lightweight user interface; a portal**: VEDA Auth Central needs a user interface where it is easy to configure VEDA applications, add application users to groups and give groups permissions to use different applications.

**Ability to configure complex authorization rules**: VEDA requires the capability to authorize users to applications based on various criteria such as profile properties, geolocation, and institutional affiliation. It is essential that the system can effectively apply these complex rules to manage and provide access, ensuring that the right users have the appropriate permissions based on their specific attributes.

**Easy application configurations through templates**: To simplify the application configuration process, VEDA Auth Central provides application owners with pre-built templates for common application types, such as Grafana and STAC. These templates streamlin the setup, making it easier and faster to configure applications.

**Application roles for groups**: VEDA Auth Central allows the creation of roles during application configuration process. These roles can then be assigned to groups, making it easy to manage user permissions by applying specific roles to users within those groups. This ensures that access control is both flexible and aligned with the needs of each application.

**Automatic group mapping**: Another key feature of VEDA Auth Central is automatic group mapping, where groups in the Identity Provider (IdP) are automatically mapped to corresponding groups in VEDA Auth Central. This feature can be enabled or disabled at the tenant level, providing flexibility in its implementation. Additionally, users have the option to override these automatic mappings through the portal, allowing the original group from the IdP to be mapped to a different group within VEDA Auth Central as needed.

**End-user profile management**: The portal offers end-users the ability to view and manage their profiles, view the groups they belong to and permissions they have. Users can easily edit their profile details, such as their first name, last name and other relevant information, ensuring their personal information is up-to-date and accurate.

**One portal for all**: A key objective of VEDA Auth Centralis to provide a single portal that serves all user categories effectively. VEDA leadership can use this portal to gain an overview of all appplications, while the application owners, developers, and end-users can manage and interact with the system according to their specific needs. This unified portal ensures that all users have a centralized, consistent interface for their tasks.


## Considered Options
Existing user portals and interfaces are typically tailored to their specific APIs and backend services; e.g. Keycloak portal. Integrating these with the VEDA Auth Central backend would require additional developement effort, potentially complicating the portal with unnecessary workflows and features. Therefore, creating a dedicated portal for VEDA Auth Central ensures a streamlined, purpose-built solution that anoids these complexities and provides a more efficient user experience. 


## Decision Outcome
1. Design: [**Auth Central Portal UI Designs**](https://www.figma.com/design/HPlj8Q7BHtaGzYNXRaE7if/Auth-Central?node-id=0-1&t=CH0OiVIpUhIrRybX-0)
2. Prototype: [**Auth Central Portal Prototype**](https://www.figma.com/proto/HPlj8Q7BHtaGzYNXRaE7if/Auth-Central?node-id=82-2014&starting-point-node-id=82%3A2014 
)


## Conclusion
VEDA Auth Central provides a streamlined, user-friendly solution for managing authentication and authorization across VEDA applications. The single portal supports all user categories, simplifying applicatino configuration, user management and permission control. Key features like lightweight interfaces, enforcing complex authorization rules, application templates, flexible role and group management esure efficient access contro and automatic group mapping further enhances integration with existing Indentity Providers, making VEDA Auth Central a powerful and adaptable tool for managing access with ease.


