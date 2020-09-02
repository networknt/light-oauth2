const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    app.use('/oauth2/code', createProxyMiddleware({ target: 'https://localhost:6881', secure: false }));
    app.use('/portal/command', createProxyMiddleware({ target: 'https://localhost:8441', secure: false }));
    app.use('/portal/query', createProxyMiddleware({ target: 'https://localhost:8442', secure: false }));
    app.use('/google', createProxyMiddleware({ target: 'https://local.lightapi.net', secure: false }));
    app.use('/facebook', createProxyMiddleware({ target: 'https://local.lightapi.net', secure: false }));
};
