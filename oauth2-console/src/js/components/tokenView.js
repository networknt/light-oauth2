import React from 'react';
import qs from 'querystring';
import * as Yup from 'yup';
import { Formik, Form } from 'formik';
import { InputField, InputSelect, DataViewer } from './widgets.js';
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
    grant_type: {
        placeholder: 'Grant Type',
        label: 'Grant Type'
    }
};

const TokenRequestSchema = Yup.object().shape({
    client_id: Yup.string()
                     .required(FieldDict['client_id'].label + ' is required.'),
    client_secret: Yup.string()
                     .required(FieldDict['client_secret'].label + ' is required.'),
    grant_type: Yup.string()
                     .required(FieldDict['grant_type'].label + ' is required.')
});

const TokenRequestEditor = ({send}) => (
    <div className='col-8' id='token-request-editor'>
        <Formik
            enableReinitialize
            initialValues={{
                client_id: '',
                client_secret: '',
                grant_type: '',
                code: ''
            }}

            validationSchema={TokenRequestSchema}

            onSubmit={(values, {resetForm})=>{
                send(values);
                resetForm();
            }} 

            render={({ values, errors, touched, handleSubmit, isSubmitting, resetForm }) => {
                return (   
                    <Form>
                        <InputField name='client_id' required fieldDict={FieldDict}/>
                        <InputField name='client_secret' required fieldDict={FieldDict}/>
                        <InputSelect name='grant_type' required  fieldDict={FieldDict} options={['authorization_code', 'client_credentials']}/>
                        <button type='submit' disabled={isSubmitting} className='btn btn-primary float-right'>Submit</button>
                    </Form>);
                }
            }
        />
    </div>
);

export class TokenView extends React.Component {
    constructor(props){
        super(props);

        this.state={
            token: '',
            auth_code: '',
            error: ''
        };

        this.axiosClient = Utils.createAxiosClient(); 

        this.codeQueryUrl = process.env.REACT_APP_CODE_URL
        this.postUrl = process.env.REACT_APP_TOKEN_URL
    }

    sendRequest(r){
        if (r['grant_type']==='authorization_code'){
            this.requestAuthorization(r, o=>this.requestToken(o));
        }else{
            this.requestToken(r);
        }
    }

    requestAuthorization(r, callback){
        let authStr = 'Basic ' + btoa ('admin:123456');
        let config = {
            headers:{
                Authorization: authStr
            },

            validateStatus: null,
        };

        let targetUrl = this.codeQueryUrl+ '/authorization';
        let codeQueryUrl = this.codeQueryUrl + '?response_type=code&client_id=' + r['client_id'] + '&redirect_uri=' + targetUrl;

        this.axiosClient.get(codeQueryUrl, config)
        .then(response => {
            let redirectUrl = response.request.responseURL;
            let code = redirectUrl.substring(redirectUrl.indexOf('=')+1);

            if (!Utils.isEmpty(code)){
                callback && callback(Object.assign({}, r, {code: code, redirect_uri: targetUrl}));
            }else{
                this.handleError({message: 'failed to acquire authorization code.'});
            }
        });
    }

    requestToken(r){
        let authStr = 'Basic ' + r['client_id'] + ':' + r['client_secret'];
        let config = {
          headers: {
            'Authorization': authStr,
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }

        this.axiosClient.post(this.postUrl, qs.stringify(Utils.clean(r)), config)
        .then(response => {
            const data = response.data;

            this.setState({token: data, auth_code: r['code']});
        })
        .catch(error => {
            this.handleError(error);
        });
    }

    closeViewer(){
        this.setState({token: ''});
    }

    handleError(error){
        this.setState({error: error});
    }

    renderViewer(){
        if (Utils.isEmpty(this.state.token)){
            return '';
        }

        let dataId = 'token-viewer';
        let active = true;

        let data;
        
        if (!Utils.isEmpty(this.state.auth_code)){
            data = {
                authorizationCode: this.state.auth_code,
                token: this.state.token
            };
        }else{
            data = this.state.token;
        }

        return (
            <div className='col-12 standalone'>
                <DataViewer data={data} dataId={dataId} active={active} 
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
                <TokenRequestEditor send={r=>this.sendRequest(r)}/>     
                {this.renderViewer()}
                {this.renderError()}
            </div>
        );
    }
}
