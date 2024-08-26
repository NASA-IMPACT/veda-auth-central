import os
from veda_authenticator import VedaOAuthenticator

c.JupyterHub.authenticator_class = VedaOAuthenticator

c.VedaOAuthenticator.client_id = os.getenv('OAUTH_CLIENT_ID')
c.VedaOAuthenticator.client_secret = os.getenv('OAUTH_CLIENT_SECRET')
c.VedaOAuthenticator.oauth_callback_url = 'http://localhost:8000/hub/oauth_callback'

# Custom OIDC provider endpoints
c.VedaOAuthenticator.authorize_url = 'https://api.veda.usecustos.org/api/v1/identity-management/authorize'
c.VedaOAuthenticator.token_url = 'https://api.veda.usecustos.org/api/v1/identity-management/token'
c.VedaOAuthenticator.userdata_url = 'https://api.veda.usecustos.org/api/v1/user-management/userinfo'
c.VedaOAuthenticator.userdata_method = 'GET'
c.VedaOAuthenticator.userdata_params = {"scope": "openid profile email"}
c.VedaOAuthenticator.username_key = 'email'

# Set the required OAuth2 scopes
c.VedaOAuthenticator.scope = ['openid', 'profile', 'email']

c.JupyterHub.spawner_class = 'dockerspawner.DockerSpawner'
c.DockerSpawner.container_image = 'jupyter/base-notebook:latest'
c.Spawner.notebook_dir = '~/notebooks'
c.JupyterHub.log_level = 'DEBUG'


