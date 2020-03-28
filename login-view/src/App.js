import React from 'react';
import { Switch, Route } from 'react-router-dom';
import Login from './components/Login';
import ResetPassword from './components/ResetPassword';
import ForgetPassword from './components/ForgetPassword';

function App() {
  return (
    <Switch>
      <Route exact path="/" component={Login} />
      <Route exact path="/reset" component={ResetPassword} />
      <Route exact path="/forget" component={ForgetPassword} />
    </Switch>
  )
}

export default App;
