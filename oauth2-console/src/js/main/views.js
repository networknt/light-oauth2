import React from 'react';
import axios from 'axios';
import $ from 'jquery';
import * as Yup from 'yup';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import './views.css';

//presentation
const toServiceSummaryId = serviceId => serviceId+'-s';

const toServiceDetailId = serviceId => serviceId+'-d';

const capitalize = s => s && s[0].toUpperCase() + s.slice(1).toLowerCase();

const isEmpty = s => !s || 0 === s.length;

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

const ServiceDetailViewer = ({service, active, close, edit, remove}) => (
    <div className='tab-pane' id={toServiceDetailId(service.serviceId)} role='tabpanel'>
        <button type="button" className="icon-button" aria-label="Close" onClick={()=>close()}>
            <i className="material-icons">close</i>
        </button>
        <button type="button" className="icon-button" aria-label="Delete" onClick={service=>remove(service)}>
            <i className="material-icons">delete_sweep</i>
        </button>
        <button type="button" className="icon-button" aria-label="Edit" onClick={service=>edit(service)}>
            <i className="material-icons">create</i>
        </button>

        <pre>{JSON.stringify(service, null, 4)}</pre>
    </div>
);

const FieldDict = {
    serviceId: {
        placeholder: 'Service ID',
        label: 'Service ID'
    },
    serviceType: {
        placeholder: 'Service Type',
        label: 'Service Type'
    },
    serviceName: {
        placeholder: 'Service Name',
        label: 'Service Name'
    },
    serviceDesc: {
        placeholder: 'Service Description',
        label: 'Service Description'
    },
    scope: {
        placeholder: 'Scope',
        label: 'Scope'
    },
    ownerId: {
        placeholder: 'Owner ID',
        label: 'Owner ID'
    } 
};

const InputField = ({name, required}) => (
    <div className={`form-group ${required?'required':''}`}>
        <label htmlFor={name} className='control-label'>{FieldDict[name].label}</label>
        <Field className='form-control' name={name} placeholder={FieldDict[name].placeholder}/>
        <ErrorMessage name={name} render={msg => <div className='error-message pl-1'>{msg}</div>}/>
    </div>
);

const InputTextArea = ({name, required}) => (
    <div className={`form-group ${required?'required':''}`}>
        <label htmlFor={name} className='control-label'>{FieldDict[name].label}</label>
        <Field className='form-control' component='textarea' name={name} placeholder={FieldDict[name].placeholder}/>
        <ErrorMessage name={name} render={msg => <div className='error-message pl-1'>{msg}</div>}/>
    </div>
);

const InputSelect = ({name, required, options}) => (
    <div className={`form-group ${required?'required':''}`}>
        <label htmlFor={name} className='control-label'>{FieldDict[name].label}</label>
        <Field className='form-control' component='select' name={name}>
            <option value=''> Please select </option>
            {
                options.map((item, index) => <option key={index} value={item}> {capitalize(item)} </option>)
            }
        </Field>
        <ErrorMessage name={name} render={msg => <div className='error-message pl-1'>{msg}</div>}/>
    </div>
);

// data, controller & form
const VIEW_MODE='view',
      EDIT_MODE='edit',
      ADD_MODE='add';

const ServiceSchema = Yup.object().shape({
    serviceId: Yup.string()
                    .max(32, FieldDict['serviceId'].label + ' cannot be longer than ${max}')
                    .required(FieldDict['serviceId'].label + ' is required.')
                    .test(
                        'validServiceId',
                        '${path} is not unique',
                        function (value) {
                            if (!value || (value !== null && value.length === 0)) {
                                return false;
                            }

                            const {context} = this.options;
                            const existingIds = context.existingIds;
                            const result = !existingIds || !existingIds.includes(value);
                            return result;
                        }),
    serviceType: Yup.string()
                     .max(16, FieldDict['serviceType'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['serviceType'].label + ' is required.'),
    serviceName: Yup.string()
                     .max(32, FieldDict['serviceName'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['serviceName'].label + ' is required.'),
    serviceDesc: Yup.string()
                     .max(1024, FieldDict['serviceDesc'].label + ' cannot be longer than ${max}'),
    scope: Yup.string()
                     .max(1024, FieldDict['scope'].label + ' cannot be longer than ${max}'),
    ownerId: Yup.string()
                    .max(32, FieldDict['ownerId'].label + ' cannot be longer than ${max}')
                    .required(FieldDict['ownerId'].label + ' is required.')
});

class ServiceDetailEditor extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            userIds: [],
            postResult: ''
        }

        this.userQueryUrl = process.env.REACT_APP_USERS_URL + process.env.REACT_APP_DEFAULT_PAGE_ARG;
    }

    componentDidMount(){/*
        axios.get(this.userQueryUrl, {Origin:window.location.origin}) //set 'Origin' header to meet CORS requirements
        .then(response => {
            const userIds = response.data.slice().map(user=>user.userId);

            this.setState({userIds: userIds});
        });*/
    }

    doSubmit(values){
        this.props.save && this.props.save(values);
    }

    doClose(){
        this.props.close && this.props.close();
    }

    render(){
        let initialValues = this.props.service || {
            serviceId: '',
            serviceType: '',
            serviceName: '',
            serviceDesc: '',
            scope: '',
            ownerId: ''
        };
        
        return (
            <div className={this.props.className} id='service-detail-editor'>
                <Formik
                    enableReinitialize
                    initialValues={initialValues}

                    validationSchema={ServiceSchema}

                    onSubmit={(values, {resetForm})=>{
                        this.doSubmit(values);
                        resetForm();
                    }} 

                    render={({ errors, touched, handleSubmit, isSubmitting, resetForm }) => {
                        let ownerIdInput = <InputSelect name="ownerId" required options={this.state.userIds}/>;

                        if (isEmpty(this.state.userIds)){
                            ownerIdInput = <InputField name="ownerId" required/>;
                        }

                        return (   
                            <Form>
                                <button type="button" className="icon-button" aria-label="Close" 
                                    onClick={()=>{
                                                    resetForm();
                                                    this.doClose();
                                                   }}>
                                    <i className="material-icons">close</i>
                                </button>
                                <InputField name="serviceId" required/>
                                <InputSelect name="serviceType" required options={['swagger', 'openapi', 'graphql', 'hybrid']}/>
                                <InputField name="serviceName" required/>
                                <InputTextArea name="serviceDesc"/>
                                <InputTextArea name="scope"/>
                                {ownerIdInput}
                                <button type="submit" disabled={isSubmitting} className="btn btn-primary float-right">Submit</button>
                            </Form>);
                        }
                    }
                />
            </div>
        );
    }
}


export class Service extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            mode: VIEW_MODE,
            activeServiceId: '',
            services: []
        }

        this.serviceQueryUrl = process.env.REACT_APP_SERVICES_URL + process.env.REACT_APP_DEFAULT_PAGE_ARG;
        this.serviceClient = axios.create({ 
                validateStatus: function (status) {
                                    return status === 200;
                                },
                Origin:window.location.origin
        });
    }
    
    componentDidMount(){
        this.refresh();
    }

    refresh(){
        this.serviceClient.get(this.serviceQueryUrl, {Origin:window.location.origin}) //set 'Origin' header to fit CORS requirements
        .then(response => {
            const services = response.data.slice();

            this.setState({
                mode: VIEW_MODE,
                activeServiceId: '',
                services: services});
        });
    }

    addService(service){
        this.serviceClient.post(process.env.REACT_APP_SERVICES_URL, service)
        .then(response => {
            this.refresh();
        })
        .catch(error => {
        });
    }

    updateService(service){
        this.serviceClient.put(process.env.REACT_APP_SERVICES_URL, service)
        .then(response => {
            this.refresh();
        })
        .catch(error => {
        });
    }

    removeService(service){
        if (service){
            let deleteUrl = process.env.REACT_APP_SERVICES_URL + '/' + service.serviceId

            this.serviceClient.delete(deleteUrl)
            .then(response => {
                this.refresh();
             });
        }
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

    showEditor(mode){
        // toggle the viewer first
        if (this.state.activeServiceId){
            toggleTab(this.state.activeServiceId, false);
        }

        const copiedState = Object.assign({}, this.state);
        copiedState.mode=mode;

        if (ADD_MODE===mode){
            copiedState.activeServiceId='';
        }

        this.setState(copiedState); 
    }

    renderServiceDetailEditor(){
        const cssClasses = this.state.mode===VIEW_MODE?'hide':'show';

        let serviceEditor = <ServiceDetailEditor className={cssClasses} save={s=>this.addService(s)} close={()=>this.refresh()}/>;

        if (EDIT_MODE===this.state.mode){
            let service = this.state.services.find(s=>s.serviceId===this.state.activeServiceId);
            serviceEditor = <ServiceDetailEditor className={cssClasses} save={s=>this.updateService(s)} service={service} close={()=>this.refresh()}/>;
        }

        return serviceEditor;
    }

    renderServiceDetailViewer(){
        const cssClasses = 'tab-content ' + (this.state.mode===VIEW_MODE?'show':'hide');

        return (<div className={cssClasses} id='service-detail-viewer'>
                {
                    this.state.services.map((service, index) =>
                        <ServiceDetailViewer key={index} service={service} active={service.serviceId===this.state.activeServiceId} 
                            close={()=>this.toggleDetailView(service.serviceId)}
                            edit={s=>this.showEditor(EDIT_MODE)}
                            remove={s=>this.removeService(service)}
                        />)
                }
                </div>);
    }
    
    render(){
        return (
            <div className='row' id='serviceView'>
                <div className='col-4'>
                    <div className="mb-2 d-flex flex-row-reverse">
                        <button type="button" className="btn btn-outline-primary" onClick={()=>this.showEditor(ADD_MODE)}>Add service</button>
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
