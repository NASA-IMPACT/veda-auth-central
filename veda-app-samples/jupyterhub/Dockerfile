# Use the official JupyterHub image
FROM jupyterhub/jupyterhub:3.0

# Install additional dependencies
RUN pip install oauthenticator requests pyjwt
RUN pip install dockerspawner

# Copy the configuration file and custom authenticator into the image
COPY jupyterhub_config.py /srv/jupyterhub/jupyterhub_config.py
COPY veda_authenticator.py /srv/jupyterhub/veda_authenticator.py

# Set environment variables
ENV JUPYTERHUB_CONFIG=/srv/jupyterhub/jupyterhub_config.py
ENV PYTHONPATH=/srv/jupyterhub

# Expose port 8000 for JupyterHub
EXPOSE 8000

# Start JupyterHub
CMD ["jupyterhub"]
