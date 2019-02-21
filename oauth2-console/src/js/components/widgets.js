import React from 'react';
import {Field, ErrorMessage } from 'formik';
import Utils from '../common/utils.js';
import './editor.css';

export const LabelItem = ({dataId, active, itemName, onClick}) => {
    let cssClass = 'list-group-item list-group-item-action flex-column align-items-start ' + (active?'active':'');

    return (
       <a className={cssClass}
            data-toggle='list' 
            href={'#'+Utils.toViewerId(dataId)} 
            role='tab' 
            id={Utils.toItemId(dataId)}
            onClick={()=>onClick()}>

            {itemName}
        </a>
    );
}

export const JSONViewer = ({data, dataId, active, hideFields, close, edit, remove, additionalControls}) => {
    if (!active){ // simple return an empty tab-pane to establish the linkage with list items
        return (
            <div className='tab-pane' id={Utils.toViewerId(dataId)} role='tabpanel'/>
        );
    }

    let displayData = Object.assign({}, data);

    if (!Utils.isEmpty(hideFields)){
        hideFields.forEach(f=>delete displayData[f]);
    }

    let removeControl;

    if (remove){
        removeControl=<button type="button" title='remove' className="icon-button" aria-label="Delete" onClick={data=>remove(data)}>
                <i className="material-icons">delete_sweep</i>
            </button>;
    }

    let editControl;

    if (edit){
        editControl=<button type="button" title='edit' className="icon-button" aria-label="Edit" onClick={data=>edit(data)}>
                <i className="material-icons">create</i>
            </button>;
    }


    return (
        <div className='tab-pane fade show active' id={Utils.toViewerId(dataId)} role='tabpanel'>
            <button type="button" title='close' className="icon-button" aria-label="Close" onClick={()=>close()}>
                <i className="material-icons">clear</i>
            </button>
            {removeControl}
            {editControl}

            {additionalControls}

            <textarea readOnly value={JSON.stringify(displayData, null, 4)} />
        </div>
    );
}

export const InputField = ({name, type, required, disabled, fieldDict}) => (
    <div className={`form-group ${required?'required':''}`}>
        <label htmlFor={name} className='control-label'>{fieldDict[name].label}</label>
        <Field className='form-control' name={name} type={type} disabled={disabled} placeholder={fieldDict[name].placeholder}/>
        <ErrorMessage name={name} render={msg => <div className='error-message pl-1'>{msg}</div>}/>
    </div>
);

export const InputTextArea = ({name, required, fieldDict}) => (
    <div className={`form-group ${required?'required':''}`}>
        <label htmlFor={name} className='control-label'>{fieldDict[name].label}</label>
        <Field className='form-control' component='textarea' name={name} placeholder={fieldDict[name].placeholder}/>
        <ErrorMessage name={name} render={msg => <div className='error-message pl-1'>{msg}</div>}/>
    </div>
);

export const InputSelect = ({name, required, options, fieldDict}) => (
    <div className={`form-group ${required?'required':''}`}>
        <label htmlFor={name} className='control-label'>{fieldDict[name].label}</label>
        <Field className='form-control' component='select' name={name}>
            <option value=''> Please select </option>
            {
                options.map((item, index) => <option key={index} value={item}> {Utils.replace(Utils.capitalize(item), '_', ' ')} </option>)
            }
        </Field>
        <ErrorMessage name={name} render={msg => <div className='error-message pl-1'>{msg}</div>}/>
    </div>
);

