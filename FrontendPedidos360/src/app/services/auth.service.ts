import { Injectable } from '@angular/core';
import { MsalService } from '@azure/msal-angular';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private msalService: MsalService) {}

  getRoles(): string[] {
    const account = this.msalService.instance.getActiveAccount();
    if (!account) return [];
    const roles = account.idTokenClaims?.['roles'] as string[];
    return roles || [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  isAdmin(): boolean { return this.hasRole('Admin'); }
  isOperador(): boolean { return this.hasRole('Operador'); }
  isCliente(): boolean { return this.hasRole('Cliente'); }
}
