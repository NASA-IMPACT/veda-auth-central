# Veda React App

This project provides a minimal setup to integrate veda auth in a React app.

### Steps to run the project
After cloning the project, you need to update the config in `auth.js` file with your Veda app's client id and client secret.

**Note**: The client secret must never be exposed in the frontend code in a real-world scenario.

<br>After the config is updated, you can run the following commands to start the project:
```
    yarn install
    yarn run dev
```

* After running the above commands, go to `http://localhost:5173` or `http://localhost:5173/login` to see the login page.
* Click on the Institution Login button to start the login flow.
* You'll be redirected to the Veda login page.
* After successful login, you'll be redirected back to the app on the user-info page.

