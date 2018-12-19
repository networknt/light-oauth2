import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Views } from '../common/common.js';

const Main = () => (
  <main>
    <Switch>
        <Route exact path='/' component={Views[0].component}/>
        {
            Views.map((view, index) => <Route exact path={view.path} component={view.component} key={view.name}/>)
        }
    </Switch>
  </main>
)

export default Main;
