import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { NavbarComponent } from '../navbar/navbar.component';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit {
  orders: any[] = [];
  cargando: boolean = true;
  error: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarPedidosReales();
  }

  cargarPedidosReales(): void {
  this.cargando = true;
  this.error = '';

  const url = `${environment.apiUrl}/api/orders`;
  console.log('URL que se va a llamar:', url); // LOG TEMPORAL

  this.http.get<any[]>(url).subscribe({
    next: (datos) => {
      console.log('Pedidos reales desde AWS:', datos);
      this.orders = datos;
      this.cargando = false;
    },
    error: (err) => {
      console.error('Error al obtener pedidos desde AWS:', err);
      this.error = 'No se pudieron cargar los pedidos.';
      this.cargando = false;
    }
  });
}
}