import { createRoot } from 'react-dom/client';
import App from './App';
import { extendTheme, ChakraProvider } from '@chakra-ui/react'
import { AuthProvider, AuthProviderProps } from "react-oidc-context";
import localOidcConfig from './lib/localOidcConfig.json';
import prodOidcConfig from './lib/prodOidcConfig.json';
import { BACKEND_URL, CLIENT_ID } from './lib/constants';
import { WebStorageStateStore } from 'oidc-client-ts';

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

let theOidcConfig;
let redirect_uri:string;

if (!process.env.NODE_ENV || process.env.NODE_ENV === 'development') {
  theOidcConfig = localOidcConfig;
  redirect_uri = 'http://localhost:5173/oauth-callback';
} else {
  // production code
  theOidcConfig = prodOidcConfig;
  redirect_uri = 'https://veda.usecustos.org/oauth-callback';
}

const theConfig:AuthProviderProps = {
  authority: `${BACKEND_URL}/api/v1/identity-management/`,
  client_id: CLIENT_ID,
  redirect_uri: redirect_uri,
  response_type: "code",
  scope: "openid email",
  metadata: {
    authorization_endpoint: theOidcConfig.authorization_endpoint,
    token_endpoint: theOidcConfig.token_endpoint,
    revocation_endpoint: theOidcConfig.revocation_endpoint,
    introspection_endpoint: theOidcConfig.introspection_endpoint,
    userinfo_endpoint: theOidcConfig.userinfo_endpoint,
    jwks_uri: theOidcConfig.jwks_uri,
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
