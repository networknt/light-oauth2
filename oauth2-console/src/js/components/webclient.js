import React from 'react';
import { LabelItem } from './widgets.js';
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
        this.axiosClient = Utils.createAxiosClient(); 

        this.queryUrl = props.queryUrl;
        this.postUrl = props.postUrl;
        this.putUrl = props.putUrl;
        this.dataType = props.dataType.toLowerCase();
        this.viewId = this.dataType + '-view';
        this.getId = props.getId;
        this.getName = props.getName;
        this.getDeleteUrl = props.getDeleteUrl;
        this.viewer = props.viewer;
        this.editor = props.editor;
        this.hideFields = props.hideFields;

        // state
        this.state = {
            loading: true,
            showSpinner: false,
            activeId: '',
            mode: WebClient.MODES.VIEW,
            data: [],
            error: '',
            postResponse: ''
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
                loading: false,
                error: ''});

           callback && callback();
        })
        .catch(error=>{
            this.setState({
                activeId: '',
                mode: WebClient.MODES.VIEW,
                loading: false,
                showSpinner: false});

           this.handleError(error); 
        });

        // only show the spinner when it takes too long to load
        setTimeout(()=>this.setState(Object.assign({}, this.state, {showSpinner: true})), 500);
    }

    createDataViewer(obj, key){
        let dataId = this.getId(obj);
        let active = dataId===this.state.activeId;

        return (
            <this.viewer key={key} data={obj} dataId={dataId} active={active} 
                hideFields={this.hideFields}
                close={()=>this.closeViewer()}
                remove={()=>this.removeObject(obj)}
                edit={()=>this.openEditor(WebClient.MODES.EDIT)}
                handleError={e=>this.handleError(e)}
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
        copiedState.postResponse='';

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
        copiedState.postResponse='';

        this.setState(copiedState);
    }

    openViewer(dataId){
        const copiedState = Object.assign({}, this.state);

        copiedState.activeId=dataId;
        copiedState.mode=WebClient.MODES.VIEW;
        copiedState.postResponse='';

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

    renderDataEditor(){
        if (WebClient.MODES.VIEW===this.state.mode){
            return '';
        }

        if (WebClient.MODES.EDIT===this.state.mode){
            let obj = this.state.data.find(o=>this.getId(o)===this.state.activeId);
            return (
                    <this.editor save={s=>this.updateObject(s)} data={obj} close={()=>this.closeViewer()} handleError={e=>this.handleError(e)}/>
                );
        }

        return (
                <this.editor save={s=>this.addObject(s)} close={()=>this.closeViewer()} handleError={e=>this.handleError(e)}/>
        );
    }

    handleError(error){
        this.setState(Object.assign({}, this.state, {error: error}));
    }

    addObject(obj){
        this.axiosClient.post(this.postUrl, Utils.clean(obj))
        .then(response => {
            this.setState(Object.assign({}, this.state, {postResponse: response.data}));
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

    renderPostResponse(){
        if (!Utils.isEmpty(this.state.postResponse)){
            return (
                <this.viewer data={this.state.postResponse} dataId='post-response' active='true'
                    close={()=>this.closeViewer()}
                    handleError={e=>this.handleError(e)}
                />
            );
        }
    }

    render(){
        let clientView;

        if (this.state.loading){
            if (this.state.showSpinner){
                clientView =<div className='spinner-border text-primary m-5' role='status'>
                             <span className='sr-only'>Loading...</span>
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
                                {this.renderDataEditor()}
                                {this.renderError()}
                                {this.renderPostResponse()}
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
