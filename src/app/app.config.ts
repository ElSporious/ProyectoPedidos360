import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';

// Importaciones de MSAL
import { 
  MsalService, 
  MSAL_INSTANCE, 
  MSAL_GUARD_CONFIG, 
  MsalGuardConfiguration, 
  MSAL_INTERCEPTOR_CONFIG, 
  MsalInterceptorConfiguration, 
  MsalGuard, 
  MsalInterceptor, 
  MsalBroadcastService 
} from '@azure/msal-angular';
import { PublicClientApplication, InteractionType } from '@azure/msal-browser';

// Importación de tu archivo de entorno
import { environment } from '../environments/environment';

// Fábrica para crear la instancia principal de MSAL
export function MSALInstanceFactory(): PublicClientApplication {
  return new PublicClientApplication({
    auth: {
      clientId: environment.clientId,
      authority: `https://login.microsoftonline.com/${environment.tenantId}`,
      redirectUri: environment.redirectUri
    },
    cache: {
      cacheLocation: 'localStorage'
    }
  });
}

export function MSALGuardConfigFactory(): MsalGuardConfiguration {
  return {
    interactionType: InteractionType.Redirect,
    authRequest: {
      scopes: [`api://${environment.clientId}/access_as_user`]  // ← el scope de TU API
    },
    loginFailedRoute: '/login'
  };
}


  export function MSALInterceptorConfigFactory(): MsalInterceptorConfiguration {
  const protectedResourceMap = new Map<string, Array<string>>();
  
  const scopes = [`api://${environment.clientId}/access_as_user`];
  
  protectedResourceMap.set(
    'https://8qnm6s8z2m.execute-api.us-east-1.amazonaws.com',
    scopes
  );
  protectedResourceMap.set(
    'https://8qnm6s8z2m.execute-api.us-east-1.amazonaws.com/',
    scopes
  );

  console.log('ProtectedResourceMap:', [...protectedResourceMap.entries()]);

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap
  };
}


export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: MsalInterceptor,
      multi: true
    },
    {
      provide: MSAL_INSTANCE,
      useFactory: MSALInstanceFactory
    },
    {
      provide: MSAL_GUARD_CONFIG,
      useFactory: MSALGuardConfigFactory
    },
    {
      provide: MSAL_INTERCEPTOR_CONFIG,
      useFactory: MSALInterceptorConfigFactory
    },
    MsalService,
    MsalGuard,
    MsalBroadcastService
  ]
};