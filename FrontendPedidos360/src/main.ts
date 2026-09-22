import 'zone.js';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { PublicClientApplication } from '@azure/msal-browser';
import { environment } from './environments/environment';

const msalInstance = new PublicClientApplication({
  auth: {
    clientId: environment.clientId,
    authority: `https://login.microsoftonline.com/${environment.tenantId}`,
    redirectUri: environment.redirectUri
  },
  cache: {
    cacheLocation: 'localStorage'
  }
});

msalInstance.initialize().then(() => {
  bootstrapApplication(App, appConfig).catch((err) => console.error(err));
});