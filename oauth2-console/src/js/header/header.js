import React from 'react';
import '../../css/main.css';
import './header.css';

const views = ["Services", "Clients", "Users"];

function Logo(){
    return (
          <a className="logo-text" href="/">LIGHT OAuth 2</a>   
    );

}

function MenuItem(props){
    return (
        <li>
            <button onClick={props.onClick}>
                {props.text}
            </button>   
        </li>
    );
}

class Menu extends React.Component{
    constructor(props){
        super(props);
        this.state={
            activeViewId: 0
        };
    }

    selectMenuItem(i){
        this.setState({
            activeViewId: i
        });
    }

    renderMenuItem(i){
        return (
            <MenuItem text={views[i]} onClick={()=>this.selectMenuItem(i)} />
        );
    }

    render(){
        return (
            <div>
                <nav>
                    <ul>
                        {
                            views.map((text, index)=>this.renderMenuItem(index))
                        }
                    </ul>
                </nav>
            </div>
        );
    }
}

function Header() {
    return (
        <header role="banner">
            <Logo />   
            <Menu />
        </header>
    );
}

export default Header;
