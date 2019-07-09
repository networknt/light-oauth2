const proxy = require('http-proxy-middleware');

module.exports = function(app) {
    app.use(proxy('/oauth2/code', { target: 'https://localhost:6881', secure: false }));
};
