import React from 'react';
import FacebookLogin from 'react-facebook-login/dist/facebook-login-render-props'
import { FacebookLoginButton } from 'react-social-login-buttons';

function FbLogin({onSuccess}) {
    return (
        <FacebookLogin
        appId="603230757035427"
        autoLoad={false}
        fields="name,email"
        scope="public_profile,email"
        callback={onSuccess}
        render={renderProps => (
          <FacebookLoginButton onClick={renderProps.onClick}/>
        )}
        />
    );
  }
  
  export default FbLogin;
