// Remplacé au build de production par `fileReplacements` (angular.json). Les trois bases
// (apiUrl, authUrl, dataUrl) doivent pointer vers le déploiement réel avant mise en service.
export const environment = {
    production: true,
    apiUrl: 'http://localhost:8089/v1',
    authUrl: 'http://localhost:8089/auth',
    dataUrl: 'http://localhost:8089/data',
    version: '1.0.0'
  };
