from typing import Annotated, Any, Dict, List, Optional

import jwt
from fastapi import FastAPI, HTTPException, Security, security, status
from pydantic import Field
from pydantic_settings import BaseSettings


#
# Settings
#
class Settings(BaseSettings):
    keycloak_url: str
    keycloak_internal_url: Optional[str] = None
    keycloak_realm: str
    keycloak_client_id: str
    permitted_jwt_audiences: List[str] = ["account"]

    @property
    def keycloak_oidc_api_url(self):
        return (
            f"{self.keycloak_url}/realms/{self.keycloak_realm}/protocol/openid-connect"
        )

    @property
    def keycloak_jwks_url(self):
        base_url = self.keycloak_internal_url or self.keycloak_url
        return f"{base_url}/realms/{self.keycloak_realm}/protocol/openid-connect/certs"


settings = Settings()
jwks_client = jwt.PyJWKClient(settings.keycloak_jwks_url)  # Caches JWKS


#
# Dependencies
#
oauth2_scheme = security.OAuth2AuthorizationCodeBearer(
    authorizationUrl=f"{settings.keycloak_oidc_api_url}/auth",
    tokenUrl=f"{settings.keycloak_oidc_api_url}/token",
    scopes={
        f"example:{resource}:{action}": f"{action.title()} {resource}"
        for resource in ["doc"]
        for action in ["create", "read", "update", "delete"]
    },
)


def user_token(
    token_str: Annotated[str, Security(oauth2_scheme)],
    required_scopes: security.SecurityScopes,
):
    # Parse & validate token
    try:
        token = jwt.decode(
            token_str,
            jwks_client.get_signing_key_from_jwt(token_str).key,
            algorithms=["RS256"],
            audience=settings.permitted_jwt_audiences,
        )
    except jwt.exceptions.InvalidTokenError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials",
            headers={"WWW-Authenticate": "Bearer"},
        ) from e

    # Validate scopes (if required)
    for scope in required_scopes.scopes:
        if scope not in token["scope"]:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Not enough permissions",
                headers={
                    "WWW-Authenticate": f'Bearer scope="{required_scopes.scope_str}"'
                },
            )

    return token


#
# App
#
app = FastAPI(
    docs_url="/",
    swagger_ui_init_oauth={
        "appName": "ExampleApp",
        "clientId": settings.keycloak_client_id,
        "usePkceWithAuthorizationCodeGrant": True,
    },
)


@app.get("/my-scopes")
def scopes(user_token: Annotated[Dict[Any, Any], Security(user_token)]):
    """View auth token scopes."""
    return user_token["scope"].split(" ")


@app.get(
    "/docs",
    dependencies=[Security(user_token, scopes=["example:doc:read"])],
)
def read_doc():
    """Mock endpoint to read a doc. Requires `example:doc:read` scope."""
    return {
        "success": True,
        "details": "🚀 You have the required scope to read a doc",
    }


@app.post(
    "/docs",
    dependencies=[Security(user_token, scopes=["example:doc:create"])],
)
def create_doc():
    """Mock endpoint to create a doc. Requires `example:doc:create` scope."""
    return {
        "success": True,
        "details": "🚀 You have the required scope to create a doc",
    }
