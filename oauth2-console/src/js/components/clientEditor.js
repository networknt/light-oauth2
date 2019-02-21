import React from 'react';
import * as Yup from 'yup';
import { Formik, Form } from 'formik';
import { InputField, InputTextArea, InputSelect } from './widgets.js';
import Utils from '../common/utils.js';
import './editor.css';

const FieldDict = {
    clientType: {
        placeholder: 'Client Type',
        label: 'Client Type'
    },
    clientProfile: {
        placeholder: 'Client Profile',
        label: 'Client Profile'
    },
    clientName: {
        placeholder: 'Client Name',
        label: 'Client Name'
    },
    clientDesc: {
        placeholder: 'Client Description',
        label: 'Client Description'
    },
    redirectUri: {
        placeholder: 'Redirect URI',
        label: 'RedirectURI'
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

const ClientSchema = Yup.object().shape({
    clientType: Yup.string()
                     .max(12, FieldDict['clientType'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['clientType'].label + ' is required.'),
    clientProfile: Yup.string()
                     .max(10, FieldDict['clientProfile'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['clientProfile'].label + ' is required.'),
    clientName: Yup.string()
                     .max(32, FieldDict['clientName'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['clientName'].label + ' is required.'),
    clientDesc: Yup.string()
                     .max(2048, FieldDict['clientDesc'].label + ' cannot be longer than ${max}'),
    redirectUri: Yup.string()
                     .max(1024, FieldDict['redirectUri'].label + ' cannot be longer than ${max}'),
    scope: Yup.string()
                     .max(4096, FieldDict['scope'].label + ' cannot be longer than ${max}'),
    ownerId: Yup.string()
                    .max(32, FieldDict['ownerId'].label + ' cannot be longer than ${max}')
                    .required(FieldDict['ownerId'].label + ' is required.')
});

export class ClientEditor extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            userIds: []
        }

        this.axiosClient = Utils.createAxiosClient();
        this.userQueryUrl = process.env.REACT_APP_USERS_URL + process.env.REACT_APP_DEFAULT_PAGE_ARG;
        this.save=props.save;
        this.close=props.close;
        this.handleError=props.handleError;
    }

    componentDidMount(){
        this.axiosClient.get(this.userQueryUrl)
        .then(response => {
            const userIds = response.data.slice().map(user=>user.userId);

            this.setState({userIds: userIds});
        })
        .catch(error => {
            this.handleError(error);
        });
    }

    render(){
        let initialValues = this.props.data || {
            clientType: '',
            clientProfile: '',
            clientName: '',
            clientDesc: '',
            redirectUri: '',
            scope: '',
            ownerId: ''
        };
        
        return (
            <div className={this.props.className} id='client-detail-editor'>
                <Formik
                    enableReinitialize
                    initialValues={initialValues}

                    validationSchema={ClientSchema}

                    onSubmit={(values, {resetForm})=>{
                        let updatedObj = Object.assign({}, this.props.data, values);
                        this.save(updatedObj);
                        resetForm();
                    }} 

                    render={({ errors, touched, handleSubmit, isSubmitting, resetForm }) => {
                        let ownerIdInput = <InputSelect name='ownerId' required fieldDict={FieldDict} options={this.state.userIds}/>;

                        if (Utils.isEmpty(this.state.userIds)){
                            ownerIdInput = <InputField name='ownerId' required fieldDict={FieldDict}/>;
                        }

                        return (   
                            <Form>
                                <button type='button' className='icon-button' aria-label='Close' 
                                    onClick={()=>{
                                                    resetForm();
                                                    this.close();
                                                   }}>
                                    <i className='material-icons'>close</i>
                                </button>
                                <InputSelect name='clientType' required  fieldDict={FieldDict} options={['public', 'confidential', 'trusted', 'external']}/>
                                <InputSelect name='clientProfile' required  fieldDict={FieldDict} options={['webserver', 'mobile', 'browser', 'batch', 'service']}/>
                                <InputField name='clientName' required fieldDict={FieldDict}/>
                                <InputTextArea name='clientDesc' fieldDict={FieldDict}/>
                                <InputTextArea name='redirectUri' fieldDict={FieldDict}/>
                                <InputTextArea name='scope' fieldDict={FieldDict}/>
                                {ownerIdInput}
                                <button type='submit' disabled={isSubmitting} className='btn btn-primary float-right'>Submit</button>
                            </Form>);
                        }
                    }
                />
            </div>
        );
    }
}
