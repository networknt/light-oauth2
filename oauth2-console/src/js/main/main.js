import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Views } from '../common/common.js';

const Main = () => {
    let views = Object.values(Views);

    return (
        <div className='container-fluid mt-4'>
            <Switch>
                <Route exact path='/' component={views[0].component}/>
                {
                    views.map((view, index) => <Route exact path={view.path} component={view.component} key={view.name}/>)
                }
            </Switch>
        </div>
    );
};

export default Main;
