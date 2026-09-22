import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { AuthenticationResult } from '@azure/msal-browser';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

  constructor(
    private msalService: MsalService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Procesa la respuesta si viene de un redireccionamiento de Azure AD
    this.msalService.handleRedirectObservable().subscribe({
      next: (result: AuthenticationResult | null) => {
        if (result) {
          this.msalService.instance.setActiveAccount(result.account);
          this.router.navigate(['/dashboard']);
        } else if (this.msalService.instance.getActiveAccount()) {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (error) => console.error('Error durante la autenticación:', error)
    });
  }

  login(): void {
    this.msalService.loginRedirect({
      scopes: [`api://${environment.clientId}/access_as_user`]  // ← igual aquí
    });
  }
}