import React from 'react';
import * as Yup from 'yup';
import { Formik, Form } from 'formik';
import { InputField, InputTextArea, InputSelect, DataViewer } from './widgets.js';
import Utils from '../common/utils.js';
import './editor.css';

const FieldDict = {
    userId: {
        placeholder: 'User ID',
        label: 'User ID'
    },
    userType: {
        placeholder: 'User Type',
        label: 'User Type'
    },
    firstName: {
        placeholder: 'First Name',
        label: 'First Name'
    },
    lastName: {
        placeholder: 'Last Name',
        label: 'Last Name'
    },
    email: {
        placeholder: 'Email',
        label: 'Email'
    },
    password: {
        placeholder: 'Password',
        label: 'Password'
    },
    passwordConfirm: {
        placeholder: 'Password Confirm',
        label: 'Password Confirm'
    },
    roles: {
        placeholder: 'Roles',
        label: 'Roles'
    } 
};

Yup.match = function (key, message, func) {
    message = message || 'Values do not match';
    func = func || function (value) {
        return value === this.parent[key];
    }

    return Yup.mixed().test('match', message, func);
};

const UserSchema = Yup.object().shape({
    userId: Yup.string()
                     .max(32, FieldDict['userId'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['userId'].label + ' is required.'),
    userType: Yup.string()
                     .max(16, FieldDict['userType'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['userType'].label + ' is required.'),
    firstName: Yup.string()
                     .max(32, FieldDict['firstName'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['firstName'].label + ' is required.'),
    lastName: Yup.string()
                     .max(32, FieldDict['lastName'].label + ' cannot be longer than ${max}')
                     .required(FieldDict['lastName'].label + ' is required.'),
    email: Yup.string()
                     .email('Invalid email address')
                     .max(64, FieldDict['email'].label + ' cannot be longer than ${max}'),
    password: Yup.string()
                     .max(1024, FieldDict['password'].label + ' cannot be longer than ${max}'),
    passwordConfirm: Yup.match('password', 'Passwords do not match'),
    roles: Yup.string()
                     .max(2048, FieldDict['roles'].label + ' cannot be longer than ${max}'),
});

export class UserEditor extends React.Component {
    constructor(props){
        super(props);

        this.save=props.save;
        this.close=props.close;
    }

    render(){
        let initialValues = {
            userId: '',
            userType: '',
            firstName: '',
            lastName: '',
            email: '',
            password: '',
            passwordConfirm: '',
            roles: ''
        };

        if (this.props.data){
            initialValues = Object.assign({}, this.props.data, {
                password: '',
                passwordConfirm: ''
            });
        }
        
        return (
            <div className={this.props.className} id='user-detail-editor'>
                <Formik
                    enableReinitialize
                    initialValues={initialValues}

                    validationSchema={UserSchema}

                    onSubmit={(values, {resetForm})=>{
                        let updatedObj = Object.assign({}, this.props.data, values);
                        this.save(updatedObj);
                        resetForm();
                    }} 

                    render={({ errors, touched, handleSubmit, isSubmitting, resetForm }) => {
                        return (   
                            <Form>
                                <button type='button' className='icon-button' aria-label='Close' 
                                    onClick={()=>{
                                                    resetForm();
                                                    this.close();
                                                   }}>
                                    <i className='material-icons'>close</i>
                                </button>
                                <InputField name='userId' required fieldDict={FieldDict}/>
                                <InputSelect name='userType' required  fieldDict={FieldDict} options={['admin', 'customer', 'employee', 'partner']}/>
                                <InputField name='firstName' required fieldDict={FieldDict}/>
                                <InputField name='lastName' required fieldDict={FieldDict}/>
                                <InputField name='email' type='email' required fieldDict={FieldDict}/>
                                <InputField name='password' type='password' required fieldDict={FieldDict}/>
                                <InputField name='passwordConfirm' type='password' required fieldDict={FieldDict}/>
                                <InputTextArea name='roles' fieldDict={FieldDict}/>
                                <button type='submit' disabled={isSubmitting} className='btn btn-primary float-right'>Submit</button>
                            </Form>);
                        }
                    }
                />
            </div>
        );
    }
}

const PSWDFieldDict = {
    password: {
        placeholder: 'Password',
        label: 'Password'
    },
    newPassword: {
        placeholder: 'New Password',
        label: 'New Password'
    },
    newPasswordConfirm: {
        placeholder: 'New Password Confirm',
        label: 'New Password Confirm'
    }
};

const PSWDSchema = {
    password: Yup.string()
                     .max(1024, PSWDFieldDict['password'].label + ' cannot be longer than ${max}'),
    newPassword: Yup.string()
                     .max(1024, PSWDFieldDict['newPassword'].label + ' cannot be longer than ${max}'),
    newPasswordConfirm: Yup.match('password', 'Passwords do not match'),
}

const PasswordEditor = ({save, close}) => (
    <Formik
        enableReinitialize
        initialValues= {{
            password: '',
            newPassword: '',
            newPasswordConfirm: ''
        }}

        validationSchema={PSWDSchema}

        onSubmit={(values, {resetForm})=>{
            save(values);
            resetForm();
        }} 

        render={({ errors, touched, handleSubmit, isSubmitting, resetForm }) => {
            return (   
                <Form>
                    <button type='button' className='icon-button' aria-label='Close' 
                        onClick={()=>{
                                        resetForm();
                                        close();
                                       }}>
                        <i className='material-icons'>close</i>
                    </button>
                    <InputField name='password' type='password' required fieldDict={PSWDFieldDict}/>
                    <InputField name='newPassword' type='password' required fieldDict={PSWDFieldDict}/>
                    <InputField name='newPasswordConfirm' type='password' required fieldDict={PSWDFieldDict}/>
                    <button type='submit' disabled={isSubmitting} className='btn btn-primary float-right'>Submit</button>
                </Form>);
            }
        }
    />
);

export class UserViewer extends React.Component {
    constructor(props){
        super(props);
        this.state={
           showPasswordEditor:false
        }

        this.close=props.close;
        this.handleError=props.handleError;
        this.postUrl=process.env.REACT_APP_PASSWORD_URL + '/' + props.dataId;
        this.axiosClient = Utils.createAxiosClient(); 
    }

    openPasswordEditor(){
        this.setState(Object.assign({}, this.state, {showPasswordEditor:true}));
    }

    closePasswordEditor(){
        this.setState(Object.assign({}, this.state, {showPasswordEditor:false}));
    }

    resetPassword(obj){
        this.axiosClient.post(this.postUrl, Utils.clean(obj))
        .then(response => {
            this.closePasswordEditor();
            this.handleError();
        })
        .catch(error => {
            this.handleError(error);
        });
    }

    render(){
        if (this.state.showPasswordEditor){
            return (<PasswordEditor save={o=>this.resetPassword(o)} close={()=>this.closePasswordEditor()}/>);
        }else{
            let editorToggler = <button type='button' className='icon-button' aria-label='reset password' title='reset password' onClick={()=>this.openPasswordEditor()}>
                                    <i className='material-icons'>rotate_left</i>
                                </button>;
            return (<DataViewer additionalControls={editorToggler} {...this.props}/>);
        }
    }
}
