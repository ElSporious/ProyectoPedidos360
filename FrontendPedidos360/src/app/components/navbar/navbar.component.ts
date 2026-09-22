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

  logout(): void {
    this.msalService.logoutRedirect({
      postLogoutRedirectUri: 'http://localhost:4200/login'
    });
  }
}