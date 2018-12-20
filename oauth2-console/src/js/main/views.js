import React from 'react';

export class Service extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            services: []
        }
    }
    
    componentDidMount(){
       fetch(process.env.REACT_APP_SERVICES_URL)
        .then(results => results.json())
        .then(data => {
            let services = data.map(service => <div key="{service.serviceId}"> {service.serviceName} </div>);
            this.setState({services: services});
        });
    }
    
    render(){
        return (
            <div> {this.state.services} </div>
        );
    }
};

export const Client = () => (
    <div> Hello, clients. </div>
);

export const User = () => (
    <div> Hello, users. </div>
);
