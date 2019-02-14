import React from 'react';
import axios from 'axios';
import { LabelItem, JSONViewer } from './widgets.js';
import Utils from '../common/utils.js';

/**
* A generic web client that supports object CRUD by calling restful services. 
*
* @param dataType String - identifies the type of objects (e.g., service, user, client) 
* @param queryUrl String - the url used to load all objects
* @param postUrl String - the url used to add objects
* @param putUrl String - the url used to update objects
* @param getId function - returns the ID of a given object 
* @param getName function - returns the name of a given object
* @param getDeleteUrl function - return the URL for deleting a given object
* @param editor component - the editor used to edit data
*/
export class WebClient extends React.Component {
    static MODES = {
        VIEW: 'view',
        EDIT: 'edit',
        ADD: 'add'
    }

    constructor(props){
        super(props);
        // things that do not change
        this.axiosClient = axios.create({ 
                validateStatus: function (status) {
                                    return status === 200;
                                },
                Origin:window.location.origin     // set 'Origin' header to meet CORS requirements
        });

        this.queryUrl = props.queryUrl;
        this.postUrl = props.postUrl;
        this.putUrl = props.putUrl;
        this.dataType = props.dataType.toLowerCase();
        this.viewId = this.dataType + '-view';
        this.getId = props.getId;
        this.getName = props.getName;
        this.getDeleteUrl = props.getDeleteUrl;
        this.editor = props.editor;
        this.excludeFields = props.excludeFields;

        // state
        this.state = {
            loading: true,
            showSpinner: false,
            activeId: '',
            mode: WebClient.MODES.VIEW,
            data: [],
            error: ''
        }

    }

    componentDidMount(){
        this.refresh();
    }

    refresh(callback){
        this.axiosClient.get(this.queryUrl)
        .then(response => {
            const data = response.data.slice();

            this.setState({
                activeId: '',
                mode: WebClient.MODES.VIEW,
                data: data,
                loading: false});

           callback && callback();
        });

        // only show the spinner when it takes too long to load
        setTimeout(()=>this.setState(Object.assign({}, this.state, {showSpinner: true})), 500);
    }

    createDataViewer(obj, key){
        let dataId = this.getId(obj);
        let active = dataId===this.state.activeId;

        return (
            <JSONViewer key={key} data={obj} dataId={dataId} active={active} 
                excludeFields={this.excludeFields}
                close={()=>this.closeViewer()}
                remove={()=>this.removeObject(obj)}
                edit={()=>this.openEditor(WebClient.MODES.EDIT)}
            />
        );
    }

    renderDataViewers(){
        if (WebClient.MODES.VIEW!==this.state.mode){
            return '';
        }
        
        return (
            <div className='tab-content' id='data-viewer'>
                {this.state.data.map((obj, index)=>this.createDataViewer(obj, index))}
            </div>
        );
    }

    openEditor(mode){
        const copiedState = Object.assign({}, this.state);
        copiedState.mode=mode;

        if (WebClient.MODES.ADD===mode){
            copiedState.activeServiceId='';
        }   

        this.setState(copiedState); 
    }

    closeViewer(){
        //this.toggleViewer(this.viewId, this.state.activeId, false);
        const copiedState = Object.assign({}, this.state);

        copiedState.activeId='';
        copiedState.mode=WebClient.MODES.VIEW;

        this.setState(copiedState);
    }

    openViewer(dataId){
        const copiedState = Object.assign({}, this.state);

        copiedState.activeId=dataId;
        copiedState.mode=WebClient.MODES.VIEW;

        this.setState(copiedState);
    }

    createLabelItem(obj, index){
        let dataId = this.getId(obj);
        let name = this.getName(obj);
        let active = dataId===this.state.activeId;

        return (
            <LabelItem key={index} dataId={dataId} itemName={name} active={active} onClick={()=>this.openViewer(dataId)}/>
        );
    }

    renderDataEditor(Editor){
        if (WebClient.MODES.VIEW===this.state.mode){
            return '';
        }

        if (WebClient.MODES.EDIT===this.state.mode){
            let obj = this.state.data.find(o=>this.getId(o)===this.state.activeId);
            return (
                    <Editor save={s=>this.updateObject(s)} data={obj} close={()=>this.closeViwer()}/>
                );
        }

        return (
                <Editor save={s=>this.addObject(s)} close={()=>this.closeViewer()}/>
        );
    }

    handleError(error){
        this.setState(Object.assign({}, this.state, {error: error}));
    }

    addObject(obj){
        this.axiosClient.post(this.postUrl, Utils.clean(obj))
        .then(response => {
            this.refresh();
        })
        .catch(error => {
            this.handleError(error);
        });
    }

    updateObject(obj){
        this.axiosClient.put(this.putUrl, Utils.clean(obj))
        .then(response => {
            this.refresh(()=>this.openViewer(this.getId(obj)));
        })
        .catch(error => {
            this.handleError(error);
        });
    }

    removeObject(obj){
        if (obj){
            let deleteUrl = this.getDeleteUrl(obj); 

            deleteUrl && this.axiosClient.delete(deleteUrl)
            .then(response => {
                this.refresh();
             })
            .catch(error => {
                this.handleError(error);
            });
        }
    }

    renderError(){
        if (!Utils.isEmpty(this.state.error)){
            return (
               <div className='error-message'> {this.state.error.message} </div>
            );
        }
    }

    render(){
        let clientView;

        if (this.state.loading){
            if (this.state.showSpinner){
                clientView =<div class='spinner-border text-primary m-5' role='status'>
                             <span class='sr-only'>Loading...</span>
                            </div>
            }
        }else{
            clientView = <div className='row' id={this.viewId}>
                            <div className='col-4'>
                                <div className='mb-2 d-flex flex-row-reverse'>
                                    <button type='button' className='btn btn-outline-primary' onClick={()=>this.openEditor(WebClient.MODES.ADD)}>Add {this.dataType}</button>
                                </div>
                                <div className='list-group' id='client-summary-list' role='tablist'>
                                    {
                                        this.state.data.map((obj, index) => this.createLabelItem(obj, index))
                                    }
                                </div>
                            </div>
                            <div className='col-8'>
                                {this.renderDataViewers()}
                                {this.renderDataEditor(this.editor)}
                                {this.renderError()}
                            </div>
                        </div>
        }

        return (
             <div>
                {clientView}
            </div>
        );
    }
};
