const { generateKeyPairSync } = require('crypto');
const fs = require('fs');

const { publicKey, privateKey } = generateKeyPairSync('rsa', {
    modulusLength: 2048,
});

const cert = require('selfsigned').generate(
    [{ name: 'commonName', value: 'localhost' }],
    { days: 365 }
);

fs.writeFileSync('localhost-key.pem', cert.private);
fs.writeFileSync('localhost-cert.pem', cert.cert);

console.log('Certificate generated: localhost-key.pem and localhost-cert.pem');