# Keycloak + FastAPI

This code intends to serve as an example of:

- using Keycloak to manage:
  - associating users to groups
  - associating roles to groups
  - associating scopes to roles
  - associating scopes to clients
  - injecting roles into JWTs based on requesting user
- validating JWTs and scopes when processing requests made to FastAPI endpoints

## Usage

```sh
docker compose up
```

Access the UI at http://localhost:8000/. Two users are enabled: `alice` and `bob`, passwords for each user matches their username. `alice` represents an admin who is (upon request) able to retrieve all scopes. `bob` represents a regular user who is only able to retrieve the `example:doc:read`.

### Keycloak

Keycloak should be accessible on http://localhost:8080.

**Username:** `admin`
**Password:** `admin`

#### Scopes

- `example:doc:create`
- `example:doc:read`
- `example:doc:update`
- `example:doc:delete`

All scopes are set created with:

- `Type`: `None`
- `Include in token scope`: `On`
- `Display on consent screen`: `On`

#### Clients

A single client is available: `example-api`. It has all of the above scopes associated with it. The `example:doc:read` scope is marked as `Default` while all other scopes are marked as `Optional`, meaning that they will only be included in the auth token if specifically requested at time of login.

#### Roles

- `Doc Editor`
  - Assigned Scopes:
    - `example:doc:create`
    - `example:doc:read`
    - `example:doc:update`
    - `example:doc:delete`
- `Doc Reader`
  - Assigned Scopes:
    - `example:doc:read`

#### Groups

With a system this simple, it may seem unnecessary to utilize Groups to associate Users to Roles, however the intention is for this example to be illustrative of a much larger system.

- `Admin`
  - Assigned Roles:
    - `Doc Editor`
- `Standard User`
  - Assigned Roles:
    - `Doc Reader`
