import React from 'react';
import { withRouter } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { Views } from '../common/common.js';
import Utils from '../common/utils.js';
import '../../css/default.css';
import './header.css';

class Header extends React.Component{
    constructor(props){
        super(props);
        this.views = Object.values(Views);
        
        let view = props.location.pathname;

        if (!Utils.isEmpty(view)){
            view = view.substring(1).trim().toLowerCase();

            this.state = {
                activeView: view
            };
        }else{
            this.state = {
                activeView: this.views[0].name.toLowerCase()
            };
        }

    }

    handleClick(viewName){
        var newState = Object.assign({}, this.state, {activeView: viewName});        

        this.setState(newState);
    }

    render(){
        return (
            <header role='banner'>
                <div>
                    <Link to='/' className='logo-text'>LIGHT OAuth 2</Link>
                    <nav>
                        <ul>
                            {
                                this.views.map((view, index) => <li key={view.name}><Link to={view.path} 
                                                            className={this.state.activeView===view.name.toLowerCase()?'selected':''}
                                                            onClick={()=>this.handleClick(view.name.toLowerCase())}>{view.name}</Link></li>)
                            }
                        </ul>
                    </nav>
                    <a href='https://www.networknt.com/tutorial/oauth/' className='help ml-auto'>
                        <i className='material-icons'>help_outline</i>
                    </a>
                </div>
            </header>
        );
    }
}

export default withRouter(Header);
