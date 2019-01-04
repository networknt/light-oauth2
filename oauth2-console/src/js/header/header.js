import React from 'react';
import { Link } from 'react-router-dom';
import { Views } from '../common/common.js';
import '../../css/default.css';
import './header.css';

class Header extends React.Component{
    constructor(props){
        super(props);
        this.state = {
            activeView: Views[0].name
        };
    }

    handleClick(viewName){
        var newState = Object.assign({}, this.state, {activeView: viewName});        

        this.setState(newState);
    }

    render(){
        return (
            <header role="banner">
                <div>
                    <Link to='/' className="logo-text">LIGHT OAuth 2</Link>
                    <nav>
                        <ul>
                            {
                                Views.map((view, index) => <li key={view.name}><Link to={view.path} 
                                                            className={this.state.activeView===view.name?"selected":""}
                                                            onClick={()=>this.handleClick(view.name)}>{view.name}</Link></li>)
                            }
                        </ul>
                    </nav>
                </div>
            </header>
        );
    }
}

export default Header;
