import {fetchToken} from "../../api/auth.js";
import {useEffect} from "react";
import {useNavigate} from "react-router-dom";

const CallbackPage = () => {
    const navigate = useNavigate();
    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        console.log("CODE: " + code);
        if (code !== null && code !== "") {
            fetchToken({code}).then((tokenResponse) => {
                console.log("TOKEN DATA: ", JSON.stringify(tokenResponse));
                sessionStorage.setItem("access_token", tokenResponse.access_token);
                navigate('/user-info');
            });
        }
    }, [navigate]);

    return (
        <div>
            <h1>Redirecting</h1>
        </div>
    );
}

export default CallbackPage;
