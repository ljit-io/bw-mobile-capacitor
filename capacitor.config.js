const fs = require('fs');
const path = require('path');
const AppUrlConfig = require('./app-url-config.json');

const product = process.env.PRODUCT;

const cfg = AppUrlConfig[product];

fs.writeFileSync(
  path.join(__dirname, 'www', 'url-config.json'),
  JSON.stringify({ urls: cfg.urls }, null, 2) + '\n'
);

const hostnames = cfg.urls.map(u => new URL(u).hostname);

const { urls, ...capConfig } = cfg;
capConfig.server = {
  ...(capConfig.server || {}),
  allowNavigation: hostnames
};

module.exports = capConfig;
