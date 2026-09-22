import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  roles: string[] = [];
  isAdmin = false;
  isOperador = false;
  isCliente = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.roles = this.authService.getRoles();
    this.isAdmin = this.authService.isAdmin();
    this.isOperador = this.authService.isOperador();
    this.isCliente = this.authService.isCliente();
  }
}
