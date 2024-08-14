import json
import urllib.request
from typing import Annotated, Any, Dict, List

import jwt
from fastapi import FastAPI, HTTPException, Security, security, status
from pydantic_settings import BaseSettings


#
# Settings
#
class Settings(BaseSettings):
    oidc_config_url: str
    client_id: str
    permitted_jwt_audiences: List[str] = ["account"]


def build_jwks_client(oidc_config_url: str):
    with urllib.request.urlopen(oidc_config_url) as response:
        assert (
            response.status == 200
        ), f"Request for OIDC config failed with status {response.status}"
        oidc_config = json.load(response)
        return jwt.PyJWKClient(oidc_config["jwks_uri"])


settings = Settings()
jwks_client = build_jwks_client(settings.oidc_config_url)  # Caches JWKS


#
# Dependencies
#
oauth2_scheme = security.OpenIdConnect(openIdConnectUrl=settings.oidc_config_url)


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
        "clientId": settings.client_id,
        "usePkceWithAuthorizationCodeGrant": True,
        "scopes": "openid",
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
