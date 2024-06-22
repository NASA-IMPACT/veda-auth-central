import axios from "axios";
import { Buffer } from 'buffer/';

/*This config object is just for this demo project.
In a real-world scenario, you should store client id and client secret securely as per React best practices.
Client secret must never be stored on the frontend.

The base url and redirect uri should be loaded from .env file.*/
const config = {
    clientId: "_your_client_id_",
    clientSecret: "_your_client_secret_",

    vedaAuthBaseUrl: "http://localhost:8081/api/v1",
    redirectUri: "http://localhost:5173/callback/",
}

const axiosInstance = axios.create({
    baseURL: config.vedaAuthBaseUrl,
    withCredentials: false,
    headers: {
        'Accept': '*/*',
        'Content-Type': 'application/json'
    }
});

const getClientSecret = async (clientId) => {
    const {data: {custos_client_secret}} = await axiosInstance.get(
        `/identity-management/credentials`,
        {
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('access_token')}`
            },
            params: {
                'client_id': clientId
            }
        }
    );
    return custos_client_secret;
}

const getClientAuthBase64 = async (clientId = null, clientSec = null) => {
    if (clientId === null && clientSec === null) {
        clientId = config.clientId;
        clientSec = config.clientSecret;
    } else if (clientId !== null && clientSec === null) {
        clientSec = await getClientSecret(clientId);
    }

    let clientAuthBase64 = `${clientId}:${clientSec}`;
    clientAuthBase64 = Buffer.from(clientAuthBase64).toString('base64');
    clientAuthBase64 = `Bearer ${clientAuthBase64}`
    return clientAuthBase64;
}

const fetchAuthorizationEndpoint = async () => {
    // const openIdConfigEndpoint = "/identity-management/.well-known/openid-configuration";
    const redirectUri = config.redirectUri;
    const authorizeEndpoint = `/identity-management/authorize?response_type=code&client_id=${config.clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&scope=user:email&kc_idp_hint=oidc&state=LDh1iNYlJcSujbgfFakT2iwhB6PIhgBidrFBmYNTBMw`;
    const { data: { loginURI } } = await axiosInstance.get(authorizeEndpoint,{
    });
    window.location.href = loginURI;
}

const fetchToken = async ({code}) => {
    const clientAuthBase64 = await getClientAuthBase64();

    const {data} = await axiosInstance.post("/identity-management/token", {
            code: code,
            redirect_uri: config.redirectUri,
            grant_type: 'authorization_code'
    }, {
        headers: {
            'Authorization': clientAuthBase64
        }
    });
    return data;
}

const fetchUserInfo = async () => {
    const clientAuthBase64 = await getClientAuthBase64();
    const {data} = await axiosInstance.get("/user-management/userinfo", {
        params: {
            'access_token': sessionStorage.getItem('access_token')
        },
        headers: {
            'Authorization': clientAuthBase64
        }
    });
    return data;
}

export {fetchAuthorizationEndpoint, fetchToken, fetchUserInfo}