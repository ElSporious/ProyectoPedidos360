import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { NavbarComponent } from '../navbar/navbar.component';
import { environment } from '../../../environments/environment';
import { MsalService } from '@azure/msal-angular';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.css'
})
export class CatalogComponent implements OnInit {
  products: any[] = [];
  cargando = true;
  error = '';
  busqueda = '';

  isAdmin = false;
  isOperador = false;
  isCliente = false;

  // Formulario / Modal
  mostrarModal = false;
  modoEdicion = false;
  productoForm = {
    id: null,
    name: '',
    price: 0,
    stock: 0
  };

  constructor(
    private http: HttpClient,
    private msalService: MsalService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.isOperador = this.authService.isOperador();
    this.isCliente = this.authService.isCliente();

    this.cargarProductos();
  }

  async getHeaders(): Promise<HttpHeaders> {
    const account = this.msalService.instance.getActiveAccount();
    if (!account) throw new Error('No existe cuenta activa.');

    const tokenResponse = await this.msalService.instance.acquireTokenSilent({
      scopes: [`api://${environment.clientId}/access_as_user`],
      account
    });
    return new HttpHeaders({ 'Authorization': `Bearer ${tokenResponse.accessToken}` });
  }

  async cargarProductos(): Promise<void> {
    this.cargando = true;
    this.error = '';
    try {
      const headers = await this.getHeaders();
      this.http.get<any[]>(`${environment.apiUrl}/api/catalog/products`, { headers }).subscribe({
        next: (datos) => {
          this.products = datos;
          this.cargando = false;
        },
        error: () => {
          this.error = 'No se pudieron obtener los productos del catálogo.';
          this.cargando = false;
        }
      });
    } catch {
      this.error = 'Error de autenticación.';
      this.cargando = false;
    }
  }

  get productosFiltrados(): any[] {
    if (!this.busqueda.trim()) return this.products;
    const termino = this.busqueda.toLowerCase();
    return this.products.filter(p =>
      p.name?.toLowerCase().includes(termino) ||
      p.id?.toString().includes(termino)
    );
  }

  abrirModalNuevo(): void {
    this.modoEdicion = false;
    this.productoForm = { id: null, name: '', price: 0, stock: 0 };
    this.mostrarModal = true;
  }

  abrirModalEditar(producto: any): void {
    this.modoEdicion = true;
    this.productoForm = { ...producto };
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
  }

  async guardarProducto(): Promise<void> {
    try {
      const headers = await this.getHeaders();
      if (this.modoEdicion && this.productoForm.id) {
        this.http.put(`${environment.apiUrl}/api/catalog/products/${this.productoForm.id}`, this.productoForm, { headers }).subscribe({
          next: () => {
            this.cerrarModal();
            this.cargarProductos();
          },
          error: () => { this.error = 'No se pudo actualizar el producto.'; }
        });
      } else {
        this.http.post(`${environment.apiUrl}/api/catalog/products`, this.productoForm, { headers }).subscribe({
          next: () => {
            this.cerrarModal();
            this.cargarProductos();
          },
          error: () => { this.error = 'No se pudo crear el producto.'; }
        });
      }
    } catch {
      this.error = 'Error de autenticación.';
    }
  }

  getStockBadgeClass(stock: number): string {
    if (stock <= 0) return 'badge-stock-out';
    if (stock <= 5) return 'badge-stock-low';
    return 'badge-stock-ok';
  }

  getStockBadgeText(stock: number): string {
    if (stock <= 0) return 'Agotado';
    if (stock <= 5) return `Últimas ${stock} unidades`;
    return `${stock} en stock`;
  }
}
