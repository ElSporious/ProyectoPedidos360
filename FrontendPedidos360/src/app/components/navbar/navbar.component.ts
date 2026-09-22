import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MsalService } from '@azure/msal-angular';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  userName: string = '';
  userEmail: string = '';

  constructor(private msalService: MsalService) {}

  ngOnInit(): void {
    this.loadUserData();
  }

  loadUserData(): void {
    const activeAccount = this.msalService.instance.getActiveAccount();
    if (activeAccount) {
      this.userName = activeAccount.name || 'Usuario';
      this.userEmail = activeAccount.username || '';
    }
  }

  // Genera las iniciales para el Avatar (ej: "Juan Pérez" -> "JP")
  getInitials(): string {
    if (!this.userName) return 'U';
    const names = this.userName.split(' ');
    if (names.length >= 2) {
      return `${names[0][0]}${names[1][0]}`.toUpperCase();
    }
    return this.userName.substring(0, 2).toUpperCase();
  }

  logout(): void {
    // Usar window.location.origin asegura que funcione en localhost y en AWS CloudFront
    this.msalService.logoutRedirect({
      postLogoutRedirectUri: `${window.location.origin}/login`
    });
  }
}
