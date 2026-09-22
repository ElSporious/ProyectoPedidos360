import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { NavbarComponent } from '../navbar/navbar.component';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.css'
})
export class CatalogComponent implements OnInit {
  products: any[] = [];
  cargando: boolean = true;
  error: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarProductosReales();
  }

  cargarProductosReales(): void {
    this.cargando = true;
    this.error = '';

    // Ruta actualizada al endpoint del catálogo
    this.http.get<any[]>(`${environment.apiUrl}/api/catalog/products`).subscribe({
      next: (datos) => {
        console.log('Productos reales desde AWS:', datos);
        this.products = datos;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al obtener catálogo desde AWS:', err);
        this.error = 'No se pudieron cargar los productos.';
        this.cargando = false;
      }
    });
  }
}