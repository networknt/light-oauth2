import React from 'react';
import { useGoogleLogin } from 'react-google-login';
import { GoogleLoginButton } from 'react-social-login-buttons';

const clientId = '654131058807-15p8l5r4ddlusbeavvhiin9rt2cuglh6.apps.googleusercontent.com';

function GoogleLogin({onSuccess}) {

  const onFailure = (res) => {
    console.log('Login failed: res:', res);
    alert(
      `Failed to login. Please ping this to support@lightapi.net`
    );
  };

  const { signIn } = useGoogleLogin({
    onSuccess,
    onFailure,
    clientId,
    accessType: 'offline',
    responseType: 'code'
  });

  return (
    <GoogleLoginButton onClick={signIn} />
  );
}

export default GoogleLogin;