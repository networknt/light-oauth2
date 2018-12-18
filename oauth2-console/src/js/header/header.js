import React from 'react';
import '../../css/main.css';
import './header.css';

function Header() {
    return (
	<div>
	<header role="banner">
          <a className="logo-text" href="#">LIGHT OAuth 2</a>	
		  <div>
			<nav>
				<ul>
					<li><a href="#">Services</a></li>
					<li><a href="#">Client</a></li>
					<li><a href="#">Users</a></li>
				</ul>
			</nav>
		  </div>
	</header>
	<div>
		{process.env.REACT_APP_API_URL}
	</div>
	</div>
    );
}

export default Header;
