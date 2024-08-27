import base64
import os
import logging
import requests
import jwt
from urllib.parse import urlencode
from tornado.web import HTTPError
from tornado.log import app_log
from traitlets import Bool, List, Unicode, default, validate
from oauthenticator.oauth2 import OAuthenticator, OAuthLoginHandler

log = logging.getLogger(__name__)

class VedaLoginHandler(OAuthLoginHandler):
    def authorize_redirect(self, *args, **kwargs):
        app_log.info("Received auth callback", args)
        extra_params = kwargs.setdefault('extra_params', {})
        extra_params["kc_idp_hint"] = 'oidc'
        return super().authorize_redirect(*args, **kwargs)

class VedaOAuthenticator(OAuthenticator):
    client_id = Unicode(os.environ.get("CLIENT_ID"), config=True)
    client_secret = Unicode(os.environ.get("CLIENT_SECRET"), config=True)
    authorize_url = Unicode(config=True)
    token_url = Unicode(config=True)
    oauth_callback_url = Unicode(config=True)
    scope = List(Unicode(), default_value=['openid', 'email', 'org.cilogon.userinfo'], config=True)
    allowed_groups = List(Unicode(), default_value=['VedaHubAdmin', 'VedaHubEditor'], config=True)

    @default("authorize_url")
    def _authorize_url_default(self):
        return self.authorize_url

    @default("token_url")
    def _token_url_default(self):
        return self.token_url

    @validate('scope')
    def _validate_scope(self, proposal):
        """Ensure 'openid' is requested"""
        if 'openid' not in proposal.value:
            return ['openid'] + proposal.value
        return proposal.value

    @property
    def callback_url(self):
        return self.oauth_callback_url

    async def authenticate(self, handler, data=None):
        """Authenticate user and set up auth state"""
        app_log.info("Authenticate step 1")
        code = handler.get_argument("code")

        app_log.info("Authenticate step 2: code %s", code)

        auth = f"{self.client_id}:{self.client_secret}".encode('utf-8')
        auth_encoded = base64.b64encode(auth).decode('utf-8')

        headers = {
            'Authorization': f'Basic {auth_encoded}',
            'Content-Type': 'application/x-www-form-urlencoded'
        }

        response = requests.post(
            url=self.token_url,
            data=urlencode({
                'grant_type': 'authorization_code',
                'code': code,
                'redirect_uri': self.oauth_callback_url
            }),
            headers=headers
        )

        if response.status_code != 200:
            app_log.error("Failed to get token: %s", response.text)
            raise HTTPError(response.status_code, "Failed to get token")

        token_response = response.json()
        access_token = token_response.get('access_token')
        if not access_token:
            raise HTTPError(500, "No access token in response")

        # Decode the JWT token to get user info
        try:
            payload = jwt.decode(access_token, options={'verify_signature': False})
        except jwt.PyJWTError as e:
            app_log.error("Failed to decode JWT token: %s", str(e))
            raise HTTPError(500, "Invalid token")

        # Check user groups against allowed groups
        user_groups = payload.get('groups', [])
        if not any(group in self.allowed_groups for group in user_groups):
            app_log.error("User %s is not in an allowed group", payload.get('preferred_username'))
            raise HTTPError(403, f"User is not authorized to use this hub.")

        userdict = {
            "name": payload.get('preferred_username'),
            "auth_state": {
                "token_response": token_response,
                "access_token": access_token,
                "veda_user": payload
            }
        }

        app_log.info("Authenticate step 3: userdict %s", userdict)

        return userdict

    async def pre_spawn_start(self, user, spawner):
        """Pass upstream_token to spawner via environment variable"""
        app_log.info("Calling pre_spawn_start")
        auth_state = await user.get_auth_state()
        if not auth_state:
            app_log.info("Auth state not enabled")
            return

        if not await self.is_user_authorized_to_spawn_server(user.name):
            msg = f"User {user.name} is not authorized to start a server"
            raise HTTPError(401, msg)

        app_log.info("Pre-spawn step: setting UPSTREAM_TOKEN")
        spawner.environment['UPSTREAM_TOKEN'] = auth_state['access_token']

    async def is_user_authorized_to_spawn_server(self, username):
        """Check if the user is authorized to spawn a server"""
        # Implement your logic for checking user authorization here
        app_log.info(f"Checking if user {username} is authorized.")
        return True
