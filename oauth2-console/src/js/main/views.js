import React from 'react';
import {WebClient} from '../components/webclient.js';
import {Views} from '../common/common.js';
import {ServiceEditor} from '../components/serviceEditor.js';
import {ClientEditor} from '../components/clientEditor.js';
import {UserViewer, UserEditor} from '../components/userWidgets.js';
import {JSONViewer} from '../components/widgets.js';

export const Service = () => (
    <WebClient 
        dataType={Views.SERVICE.dataType}
        queryUrl={process.env.REACT_APP_SERVICES_URL + process.env.REACT_APP_DEFAULT_PAGE_ARG} 
        postUrl={process.env.REACT_APP_SERVICES_URL}
        putUrl={process.env.REACT_APP_SERVICES_URL}
        getId={service=>service.serviceId} 
        getName={service=>service.serviceName} 
        getDeleteUrl={service=>process.env.REACT_APP_SERVICES_URL + '/' + service.serviceId}
        viewer={JSONViewer}
        editor={ServiceEditor}
    />
);

export const Client = () => (
    <WebClient 
        dataType={Views.CLIENT.dataType}
        queryUrl={process.env.REACT_APP_CLIENTS_URL + process.env.REACT_APP_DEFAULT_PAGE_ARG} 
        postUrl={process.env.REACT_APP_CLIENTS_URL} 
        putUrl={process.env.REACT_APP_CLIENTS_URL} 
        getId={client=>client.clientId} 
        getName={client=>client.clientName} 
        getDeleteUrl={client=>process.env.REACT_APP_CLIENTS_URL + '/' + client.clientId}
        viewer={JSONViewer}
        editor={ClientEditor}
    />
);

export const User  = () => (
    <WebClient 
        dataType={Views.USER.dataType}
        queryUrl={process.env.REACT_APP_USERS_URL + process.env.REACT_APP_DEFAULT_PAGE_ARG} 
        postUrl={process.env.REACT_APP_USERS_URL} 
        putUrl={process.env.REACT_APP_USERS_URL} 
        getId={user=>user.userId} 
        getName={user=>user.firstName + ' ' + user.lastName} 
        getDeleteUrl={user=>process.env.REACT_APP_USERS_URL + '/' + user.userId}
        viewer={UserViewer}
        editor={UserEditor}
        hideFields={['password', 'passwordConfirm']}
    />
);