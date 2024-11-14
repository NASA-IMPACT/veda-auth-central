# Auth related Workflows for NASA VEDA

## What is this document?

We have identified 13 [use cases](https://github.com/NASA-IMPACT/veda-auth-central/labels/use%20case) that need to be served by the authentication solution used by VEDA instances. This document extracts the common workflows that serve these use cases, so we can more easily identify how the proposed solutions will serve these specific workflows. This makes evaluation easier, as we can more easily see *who* will do *what* under each proposed solution, and make choices as a group.

For each workflow, we fill in:

1. The use cases that are served by this workflow
2. Description of this workflow
3. Who will be performing this workflow
4. How frequently will this workflow be performed
5. Links to documentation on how this workflow will be performed under both the proposed options

Once completed, this will help us make a systematic informed choice on what technical solution to pick that meets our needs.

## Glossary

Since there are many moving pieces, it is important to define some common terms that will be used through this document. We will reuse terms from [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html#Terminology) where possible.

- **Relying Party (RP)**: The service that end users are trying to log in to (JupyterHub, STAC, etc).
- **Relying Party Maintainers**: The group of people responsible for building, maintaining and configuring the relying party (JupyterHub VEDA team, STAC team, etc)
- **Authentication Service Maintainers**: The group of people responsible for maintaining the *authentication service chosen* itself.
- **End Users**: People who log in to access the different Relying Party services (JupyterHub, STAC, etc)
- **Administrative Users**: A subset of end users who have administrative power over other end users. In particular, they have group management power.

## Workflows

### Workflow 1: Configuring the upstream authentication *provider*

#### Use cases served

[All of them](https://github.com/NASA-IMPACT/veda-auth-central/labels/use%20case)

#### Description

The upstream authentication provider (CILogon, GitHub, etc) is the external account that people will use to authenticate *into* the system. This itself needs configuring, with secrets, other configuration (such as allowed idps), etc.

#### Frequency of use

Once set up, this should be *extremely* infrequently changed.

#### Who would do this task?

Authentication Service Maintainers

#### Documentation for changing this configuration

1. Keycloak -> (provide link)
2. Custos / veda-auth-central-> (provide link)

### Workflow 2: Provisioning and configuring authentication clients

#### Use cases served

[All of them](https://github.com/NASA-IMPACT/veda-auth-central/labels/use%20case)

#### Description

Each time a new service is to be deployed, it would need a client_id,
client_secret and possibly other pieces of configuration provisioned. During the course of the service's time, these may also need to be rotated or changed.

#### Frequency of use

1. Each time a new service is deployed, which is fairly infrequent
2. Whenever a service wants to rotate its secrets due to an accidental exposure. There shall be no forced rotations.
3. Whenever the service needs to change other config (due to URL changes, for example)

#### Who would do this task?

Ideally, this would be done by Relying Party Maintainers following some documentation, with a lightweight approval process from Authentication Service Maintainers.

#### Documentation for performing this workflow

1. Keycloak -> (provide link)
2. Custos / veda-auth-central-> (provide link)

### Workflow 3: Assigning users to groups via a UI

#### Use cases served

[3](https://github.com/NASA-IMPACT/veda-auth-central/issues/125), 
[5](https://github.com/NASA-IMPACT/veda-auth-central/issues/127), 
[6](https://github.com/NASA-IMPACT/veda-auth-central/issues/128), 
[11](https://github.com/NASA-IMPACT/veda-auth-central/issues/133), 
[12](https://github.com/NASA-IMPACT/veda-auth-central/issues/134), 
[13](https://github.com/NASA-IMPACT/veda-auth-central/issues/135)

#### Description

We want to be able to bucket users into various groups *manually*, via a UI. This UI should be available to a small subset of users.

#### Frequency of use

1. Each time a new group of users is onboarded
2. Whenever a particular user should be given more (or less) rights
3. Each time a group of users is *offboarded*

#### Who would do this workflow?

Administrative Users.

#### Documentation for performing this workflow

1. Keycloak -> (provide link)
2. Custos / veda-auth-central -> (provide link)

### Workflow 4: Expose roles / capabilities to services

#### Use cases served

[3](https://github.com/NASA-IMPACT/veda-auth-central/issues/125), 
[4](https://github.com/NASA-IMPACT/veda-auth-central/issues/126), 
[6](https://github.com/NASA-IMPACT/veda-auth-central/issues/128), 
[11](https://github.com/NASA-IMPACT/veda-auth-central/issues/133), 
[12](https://github.com/NASA-IMPACT/veda-auth-central/issues/134), 
[13](https://github.com/NASA-IMPACT/veda-auth-central/issues/135)

#### Description

Each service will need to know what capabilities to provide the user based on their group membership. Depending on the service, this can be done via either additional information in the user's token, or by using oauth scopes.

#### Frequency of use

This would be used every time someone logs into any of the services that use authentication. If there is a mapping between specific roles / capabilities and groups, this may change as new groups are added.

#### Who would perform this workflow?

Depending on the Relying Party, mapping groups to roles / capabilities could be performed either by the Relying Party Maintainers or by Administrative Users. List of roles / capabilities are always maintained by Relying Party Maintainers.

#### Documentation on this workflow

1. Keycloak -> (provide link)
2. Custos / veda-auth-central -> (provide link)


### Workflow 5: User Profiles for credential storage

#### Use cases served

[7](https://github.com/NASA-IMPACT/veda-auth-central/issues/129), 
[8](https://github.com/NASA-IMPACT/veda-auth-central/issues/130), 
[9](https://github.com/NASA-IMPACT/veda-auth-central/issues/131), 
[10](https://github.com/NASA-IMPACT/veda-auth-central/issues/132)

#### Description

We want to provide a place where users can store specific authentication tokens for third party services (such as DPS jobs or Earthdata login or other HPC systems). End users need a UI where they can store these tokens, and specific services that have permissions to retrieve these should be able to retrieve them.

#### Frequency of use

Based on our current set of use cases, this is only used by people who are trying to do something reasonably advanced - access external compute resources from within our compute resources. So not frequent yet.

#### Who would perform this workflow?

1. End Users would need to use this UI to enter any tokens they have
2. Relying Party Maintainers would need to configure wether they need access to any specific tokens or not.


#### Documentation on this workflow

1. Keycloak -> (provide link)
2. Custos / veda-auth-central -> (provide link)
