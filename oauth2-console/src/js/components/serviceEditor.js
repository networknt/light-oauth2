import React from 'react';
import axios from 'axios';
import * as Yup from 'yup';
import { Formik, Form } from 'formik';
import { InputField, InputTextArea, InputSelect } from './widgets.js';
import Utils from '../common/utils.js';
import './editor.css';

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

export class ServiceEditor extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            userIds: [],
            postResult: ''
        }

        this.userQueryUrl = process.env.REACT_APP_USERS_URL + process.env.REACT_APP_DEFAULT_PAGE_ARG;
        this.save = props.save;
        this.close = props.close;
    }

    componentDidMount(){
        axios.get(this.userQueryUrl, {Origin:window.location.origin}) //set 'Origin' header to meet CORS requirements
        .then(response => {
            const userIds = response.data.slice().map(user=>user.userId);

            this.setState({userIds: userIds});
        });
    }

    render(){
        let initialValues = this.props.data || {
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
                        let updatedObj = Object.assign({}, this.props.data, values);
                        this.save(updatedObj);
                        resetForm();
                    }} 

                    render={({ errors, touched, handleSubmit, isSubmitting, resetForm }) => {
                        let ownerIdInput = <InputSelect name="ownerId" required fieldDict={FieldDict} options={this.state.userIds}/>;

                        if (Utils.isEmpty(this.state.userIds)){
                            ownerIdInput = <InputField name="ownerId" required fieldDict={FieldDict}/>;
                        }

                        return (   
                            <Form>
                                <button type="button" className="icon-button" aria-label="Close" 
                                    onClick={()=>{
                                                    resetForm();
                                                    this.close();
                                                   }}>
                                    <i className="material-icons">close</i>
                                </button>
                                <InputField name="serviceId" required fieldDict={FieldDict}/>
                                <InputSelect name="serviceType" required  fieldDict={FieldDict} options={['swagger', 'openapi', 'graphql', 'hybrid']}/>
                                <InputField name="serviceName" required fieldDict={FieldDict}/>
                                <InputTextArea name="serviceDesc" fieldDict={FieldDict}/>
                                <InputTextArea name="scope" fieldDict={FieldDict}/>
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
