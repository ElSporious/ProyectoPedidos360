export const environment = {
  production: false,
  // URL de AWS API Gateway (sin / al final). 
  // Si tu compañero definió un stage (ej: /prod), agrégalo aquí sin barra al final.
  apiUrl: 'https://8qnm6s8z2m.execute-api.us-east-1.amazonaws.com', 

  // Credenciales de Microsoft Entra ID
  clientId: '2fddb73f-5c96-454a-bd1d-763c4cc15373',
  tenantId: '874f97ca-27ae-4d8a-8d52-ee96b48fd37e',
  redirectUri: 'http://localhost:4200'
};