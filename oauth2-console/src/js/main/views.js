import React from 'react';
import axios from 'axios';
import $ from 'jquery';
import './views.css';

//presentation
const toServiceSummaryId = serviceId => serviceId+'-s';

const toServiceDetailId = serviceId => serviceId+'-d';

const toggleTab = (serviceId, active) => {
    let serviceView = $('#serviceView');
    let serviceSummaryId = toServiceSummaryId(serviceId);
    let serviceDetailId = toServiceDetailId(serviceId);
    let serviceSummary = $(serviceView).find('#'+serviceSummaryId).first();
    let serviceDetail = $(serviceView).find('#'+serviceDetailId).first();

    if (active){
        serviceSummary.addClass('active');
        serviceDetail.addClass('active');
    }else{
        serviceSummary.removeClass('active');
        serviceDetail.removeClass('active');
    }
}

const ServiceSummary = ({service, active, onClick}) => (
   <a className='list-group-item list-group-item-action flex-column align-items-start' 
        data-toggle='list' 
        href={'#'+toServiceDetailId(service.serviceId)} 
        role='tab' 
        id={toServiceSummaryId(service.serviceId)}
        onClick={onClick}>

        {service.serviceName}
    </a>
);

const ServiceDetail = ({service, active, onClick}) => (
    <div className='tab-pane' id={toServiceDetailId(service.serviceId)} role='tabpanel'>
        <button type="button" className="icon-button" aria-label="Close" onClick={onClick}>
            <i className="material-icons">clear</i>
        </button>
        <button type="button" className="icon-button" aria-label="Delete">
            <i className="material-icons">delete_sweep</i>
        </button>
        <button type="button" className="icon-button" aria-label="Edit">
            <i className="material-icons">create</i>
        </button>

        <pre>{JSON.stringify(service, null, 4)}</pre>
    </div>
);

// data & controller
export class Service extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            services: {}
        }
    }
    
    componentDidMount(){
       axios.get(process.env.REACT_APP_SERVICES_URL,
                                     {Origin:window.location.origin}) //set 'Origin' header to fit CORS requirements
        .then(response => {
            let data = response.data;

            const services = {};
            
            data.forEach(service => services[service.serviceId]={
                                                                payload: service,
                                                                active: false    
                                                            });

            this.setState({services: services});
        });
    }

    handleClick(serviceId){
        const copiedState = Object.assign({}, this.state);

        const service = copiedState.services[serviceId];

        if (!service){
            console.log('service not found.');
            return;
        }

        const active = !service.active;
        toggleTab(serviceId, active);

        service['active']=active;

        this.setState(copiedState);
    }
    
    render(){
        return (
            <div className='row' id='serviceView'>
                <div className='col-4'>
                    <div className='list-group' id='svcSummaries' role='tablist'>
                        {
                            Object.entries(this.state.services).map(([serviceId, service], index) =>
                                <ServiceSummary key={index} service={service.payload} active={service.active} onClick={()=>this.handleClick(serviceId)}/>
                            )
                        }
                    </div>
                </div>
                <div className='col-8'>
                    <div className='tab-content' id='svcDetails'>
                        {
                            Object.entries(this.state.services).map(([serviceId, service], index) =>
                                <ServiceDetail key={index} service={service.payload} active={service.active} onClick={()=>this.handleClick(serviceId)}/>
                            )
                        }
                    </div>
                </div>
            </div>
        );
    }
};

//TO be completed.
export const Client = () => (
    <div> Hello, clients. </div>
);

export const User = () => (
    <div> Hello, users. </div>
);
