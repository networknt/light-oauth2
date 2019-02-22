import React from 'react';
import * as Yup from 'yup';
import { Formik, Form } from 'formik';
import { InputField, DataViewer } from './widgets.js';
import Utils from '../common/utils.js';
import './editor.css';

const FieldDict = {
    client_id: {
        placeholder: 'Client ID',
        label: 'Client ID'
    },
    client_secret: {
        placeholder: 'Client Secret',
        label: 'Client Secret'
    },
    key_id: {
        placeholder: 'Key ID',
        label: 'Key ID'
    }
};

const CertRequestSchema = Yup.object().shape({
    client_id: Yup.string()
                     .required(FieldDict['client_id'].label + ' is required.'),
    client_secret: Yup.string()
                     .required(FieldDict['client_secret'].label + ' is required.'),
    key_id: Yup.string()
                     .required(FieldDict['key_id'].label + ' is required.')
});

const CertRequestEditor = ({send}) => (
    <div className='col-8' id='token-request-editor'>
        <Formik
            enableReinitialize
            initialValues={{
                client_id: '',
                client_secret: '',
                key_id: ''
            }}

            validationSchema={CertRequestSchema}

            onSubmit={(values, {resetForm})=>{
                send(values);
                resetForm();
            }} 

            render={({ values, errors, touched, handleSubmit, isSubmitting, resetForm }) => {
                return (   
                    <Form>
                        <InputField name='client_id' required fieldDict={FieldDict}/>
                        <InputField name='client_secret' required fieldDict={FieldDict}/>
                        <InputField name='key_id' required fieldDict={FieldDict}/>
                        <button type='submit' disabled={isSubmitting} className='btn btn-primary float-right'>Submit</button>
                    </Form>);
                }
            }
        />
    </div>
);

export class KeyView extends React.Component {
    constructor(props){
        super(props);

        this.state = {
            cert: ''
        }

        this.axiosClient = Utils.createAxiosClient(); 

        this.keyQueryUrl = process.env.REACT_APP_KEY_URL
    }

    requestCert(r){
        let authStr = 'Basic ' + btoa(r['client_id'] + ':' + r['client_secret']);
        let config = {
          headers: {
            'Authorization': authStr,
          }
        }

        let queryUrl = this.keyQueryUrl + '/' + r['key_id'];

        this.axiosClient.get(queryUrl, config)
        .then(response => {
            const data = response.data;

            this.setState({cert: data});
        })
        .catch(error => {
            this.handleError(error);
        });
    }

    closeViewer(){
        this.setState({cert: ''});
    }

    handleError(error){
        this.setState({error: error});
    }

    renderViewer(){
        if (Utils.isEmpty(this.state.cert)){
            return '';
        }

        let dataId = 'cert-viewer';
        let active = true;

        return (
            <div className='col-12'>
                <DataViewer data={this.state.cert} dataId={dataId} active={active} type='text'
                    close={()=>this.closeViewer()}
                    handleError={e=>this.handleError(e)}
                />
            </div>
        );
    }

    renderError(){
        if (!Utils.isEmpty(this.state.error)){
            return (
               <div className='col-8 error-message'> {this.state.error.message} </div>
            );
        }
    }


    render(){
        return (
            <div className='row' id='token-view'>
                <CertRequestEditor send={r=>this.requestCert(r)}/>     
                {this.renderViewer()}
                {this.renderError()}
            </div>
        );
    }
}
