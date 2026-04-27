const AppUrlConfig = require('./app-url-config.json');

const product = process.env.PRODUCT;

const urlConfig = AppUrlConfig[product];

module.exports = urlConfig;
