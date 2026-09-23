import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
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

  // Formulario / Modal (Nombres corregidos para coincidir con el HTML)
  mostrarFormulario = false; 
  editando = false; 
  productoForm = {
    id: null,
    name: '',
    description: '', // <-- Añadido porque el HTML lo pide
    price: 0,
    stock: 0
  };

  constructor(
    private http: HttpClient,
    private msalService: MsalService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
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
          this.cdr.detectChanges();
        },
        error: () => {
          this.error = 'No se pudieron obtener los productos del catálogo.';
          this.cargando = false;
          this.cdr.detectChanges();
        }
      });
    } catch {
      this.error = 'Error de autenticación.';
      this.cargando = false;
      this.cdr.detectChanges();
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

  // Nombres de métodos corregidos
  abrirFormularioNuevo(): void {
    this.editando = false;
    this.productoForm = { id: null, name: '', description: '', price: 0, stock: 0 };
    this.mostrarFormulario = true;
  }

  abrirFormularioEditar(producto: any): void {
    this.editando = true;
    this.productoForm = { ...producto };
    this.mostrarFormulario = true;
  }

  cancelarFormulario(): void {
    this.mostrarFormulario = false;
  }

  async guardarProducto(): Promise<void> {
    try {
      const headers = await this.getHeaders();
      if (this.editando && this.productoForm.id) {
        this.http.put(`${environment.apiUrl}/api/catalog/products/${this.productoForm.id}`, this.productoForm, { headers }).subscribe({
          next: () => {
            this.cancelarFormulario();
            this.cargarProductos();
          },
          error: () => { this.error = 'No se pudo actualizar el producto.'; }
        });
      } else {
        this.http.post(`${environment.apiUrl}/api/catalog/products`, this.productoForm, { headers }).subscribe({
          next: () => {
            this.cancelarFormulario();
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