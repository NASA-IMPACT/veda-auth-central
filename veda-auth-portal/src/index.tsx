import { createRoot } from 'react-dom/client';
import App from './App';
import { extendTheme, ChakraProvider } from '@chakra-ui/react'
import { AuthProvider, AuthProviderProps } from "react-oidc-context";
import oidcConfig from './lib/oidcConfig.json'
import { BACKEND_URL, CLIENT_ID } from './lib/constants';
import { WebStorageStateStore } from 'oidc-client-ts';

// if (!oidcConfig) {
//   console.error('OIDC configuration not found');
// } else {
//   console.log('OIDC configuration found', oidcConfig);
// }

const theme = extendTheme({
  colors: {
    default: {
      "default": "#1E1E1E",
      "secondary": "#757575",
      "tertiary": "#B3B3B3"
    },
    border: {
      neutral: {
        "default": "#303030",
        "secondary": "#767676",
        "tertiary": "#B2B2B2",
      }
    }
  },
});

const theConfig:AuthProviderProps= {
  authority: `${BACKEND_URL}/api/v1/identity-management/`,
  client_id: CLIENT_ID,
  redirect_uri: "http://localhost:5173/oauth-callback",
  response_type: "code",
  scope: "openid email",
  metadata: {
    authorization_endpoint: oidcConfig.authorization_endpoint,
    token_endpoint: oidcConfig.token_endpoint,
    revocation_endpoint: oidcConfig.revocation_endpoint,
    introspection_endpoint: oidcConfig.introspection_endpoint,
    userinfo_endpoint: oidcConfig.userinfo_endpoint,
    jwks_uri: oidcConfig.jwks_uri,
  },
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  automaticSilentRenew: true,
};

const container = document.getElementById('root') as HTMLElement;
const root = createRoot(container);
root.render(
  <ChakraProvider theme={theme}>
    <AuthProvider {...theConfig}
      onSigninCallback={async (user) => {
        console.log('User signed in', user);
        window.location.href = '/groups';
      }}
    >
      <App />
    </AuthProvider>
  </ChakraProvider>
);
