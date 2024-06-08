import {useEffect} from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './LoginPage.css';
import custos_home from '../../assets/custos_home.png';
import {fetchAuthorizationEndpoint} from "../../api/auth.js";

function LoginPage() {
    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
       for(let key of urlParams.keys()) {
           console.log(key, urlParams.get(key));
       }
    }, []);

    return (
        <div className="container">
            <div className="row align-items-start justify-content-center">
                <div className="col justify-content-center align-items-center">
                    <h2>Welcome to VEDA Auth Central</h2>
                    <p className="h2-sub">Sign up and start authenticating</p>
                    <img style={{width: "60%"}} src={custos_home} alt="Custos Home"/>
                    <div className="p-2 text-center">
                        <button className="btn btn-primary mt-3" onClick={fetchAuthorizationEndpoint}>Institution Login</button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;