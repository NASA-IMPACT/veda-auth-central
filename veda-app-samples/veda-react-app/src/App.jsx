import './App.css';
import {Route, BrowserRouter, Routes} from "react-router-dom";
import CallbackPage from "./pages/callback/CallbackPage.jsx";
import LoginPage from "./pages/login/LoginPage.jsx";
import UserInfoPage from "./pages/userInfo/UserInfoPage.jsx";

function App() {
  return (<BrowserRouter>
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/callback" element={<CallbackPage />} />
      <Route path="/user-info" element={<UserInfoPage />} />
    </Routes>
  </BrowserRouter>);
}

export default App
