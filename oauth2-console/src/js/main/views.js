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

const ServiceDetailViewer = ({service, active, onClick}) => (
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

class ServiceDetailEditor extends React.Component {
    constructor(props){
        super(props);
    }

    render(){
        return (
            <div className={this.props.className} id='service-detail-editor'>
                an editor
            </div>
        );
    }
}

// data & controller
const VIEW_MODE='view',
      EDIT_MODE='edit',
      ADD_MODE='add';

export class Service extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            mode: VIEW_MODE,
            activeServiceId: '',
            services: []
        }
    }
    
    componentDidMount(){
       axios.get(process.env.REACT_APP_SERVICES_URL,
                                     {Origin:window.location.origin}) //set 'Origin' header to fit CORS requirements
        .then(response => {
            const services = response.data.slice();

            this.setState({services: services});
        });
    }

    toggleDetailView(serviceId){
        const copiedState = Object.assign({}, this.state);

        if (copiedState.activeServiceId===serviceId){
            toggleTab(serviceId, false);
            copiedState.activeServiceId='';
        }else{
            toggleTab(serviceId, true);
            copiedState.activeServiceId=serviceId;
        }

        copiedState.mode=VIEW_MODE;

        this.setState(copiedState);
    }

    editService(service){
        // toggle the viewer first
        if (this.state.activeServiceId){
            toggleTab(this.state.activeServiceId, false);
        }

        const copiedState = Object.assign({}, this.state);

        if (!service){
            copiedState.mode=ADD_MODE;
            copiedState.activeServiceId='';
        }else{
            copiedState.mode=EDIT_MODE;
        }

        this.setState(copiedState); 
    }

    renderServiceDetailEditor(){
        const cssClasses = this.state.mode===VIEW_MODE?'hide':'show';

        return (
            <ServiceDetailEditor className={cssClasses}/>
        );
    }

    renderServiceDetailViewer(){
        const cssClasses = 'tab-content ' + (this.state.mode===VIEW_MODE?'show':'hide');

        return (<div className={cssClasses} id='service-detail-viewer'>
                {
                    this.state.services.map((service, index) =>
                        <ServiceDetailViewer key={index} service={service} active={service.serviceId===this.state.activeServiceId} onClick={()=>this.toggleDetailView(service.serviceId)}/>)
                }
                </div>);
    }
    
    render(){
        return (
            <div className='row' id='serviceView'>
                <div className='col-4'>
                    <div className="mb-2 d-flex flex-row-reverse">
                        <button type="button" className="btn btn-outline-primary" onClick={()=>this.editService()}>Add service</button>
                    </div>
                    <div className='list-group' id='service-summary-list' role='tablist'>
                        {
                            this.state.services.map((service, index) =>
                                <ServiceSummary key={index} service={service} active={service.serviceId===this.state.activeServiceId} onClick={()=>this.toggleDetailView(service.serviceId)}/>
                            )
                        }
                    </div>
                </div>
                <div className='col-8'>
                    { this.renderServiceDetailViewer() }
                    { this.renderServiceDetailEditor() }
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
