import React from 'react';
import axios from 'axios';
import $ from 'jquery';

function toServiceSummaryId(serviceId){
    return serviceId+'-s';
}

function toServiceDetailId(serviceId){
    return serviceId+'-d';
}

function closeTab(e, serviceId){
    let source = e.target || e.srcElement;
    let serviceView = $(source).closest('#serviceView');
    let serviceSummaryId = toServiceSummaryId(serviceId);
    let serviceDetailId = toServiceDetailId(serviceId);

    $(serviceView).find('#'+serviceSummaryId).first().removeClass('active');
    $(serviceView).find('#'+serviceDetailId).first().removeClass('active');
}

class ServiceSummary extends React.Component {
    constructor(props){
         super(props);
         this.state = {
             isActive:false
         }
    }

    closeTab(e, serviceId){
        if (this.state.isActive){
            closeTab(e, serviceId);
        }

        this.setState({isActive: !this.state.isActive});
    }

    render(){
        return (
            <a className='list-group-item list-group-item-action flex-column align-items-start' 
                data-toggle='list' 
                href={'#'+toServiceDetailId(this.props.service.serviceId)} 
                role='tab' 
                id={toServiceSummaryId(this.props.service.serviceId)}
                onClick={e=>this.closeTab(e, this.props.service.serviceId)}>

            {this.props.service.serviceName}
            </a>
     );
    }
}

const ServiceDetail = ({service}) => (
    <div className='tab-pane' id={toServiceDetailId(service.serviceId)} role='tabpanel'>
        <button type="button" className="close" aria-label="Close" onClick={e=>closeTab(e, service.serviceId)}>
            <span aria-hidden="true">&times;</span>
        </button>

        <pre>{JSON.stringify(service, null, 4)}</pre>
    </div>
);

export class Service extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            serviceSummaries: [],
            serviceDetails: []
        }
    }
    
    componentDidMount(){
       axios.get(process.env.REACT_APP_SERVICES_URL,
                                     {Origin:window.location.origin}) //set 'Origin' header to fit CORS requirements
        .then(response => {
            let data = response.data;

            let serviceSummaries = data.map(service => <ServiceSummary key={service.serviceId} service={service}/>);

            let serviceDetails = data.map(service => <ServiceDetail key={service.serviceId} service={service}/>); 

            this.setState({
                    serviceSummaries: serviceSummaries,
                    serviceDetails: serviceDetails
            });
        });
    }
    
    render(){
        return (
        <div className='row' id='serviceView'>
        <div className='col-4'>
            <div className='list-group' id='svcSummaries' role='tablist'>
                {this.state.serviceSummaries}
            </div>
        </div>
        <div className='col-8'>
            <div className='tab-content' id='svcDetails'>
                {this.state.serviceDetails}
            </div>
        </div>
        </div>
        );
    }
};

export const Client = () => (
    <div> Hello, clients. </div>
);

export const User = () => (
    <div> Hello, users. </div>
);
