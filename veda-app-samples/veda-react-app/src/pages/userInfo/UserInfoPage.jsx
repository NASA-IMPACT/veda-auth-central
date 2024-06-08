import {useEffect, useState} from "react";
import {fetchUserInfo} from "../../api/auth.js";

const UserInfoPage = () => {
    const [userInfo, setUserInfo] = useState(null);

    useEffect(() => {
        fetchUserInfo().then((userInfo) => {setUserInfo(userInfo)});
    }, []);
    return (
        <div>
            <h1>Logged in User Info</h1>
            <pre>{JSON.stringify(userInfo, null, 2)}</pre>
        </div>);
};

export default UserInfoPage;