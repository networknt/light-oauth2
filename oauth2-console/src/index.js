import React from 'react';
import ReactDOM from 'react-dom';
import Header from './js/header/header.js';

class OAuth2Console extends React.Component {
  render() {
    return (
      <Header />
    );
  }
}

// ========================================

ReactDOM.render(
  <OAuth2Console />,
  document.getElementById('root')
);

