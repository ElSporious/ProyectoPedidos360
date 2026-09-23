import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { NavbarComponent } from '../navbar/navbar.component';
import { environment } from '../../../environments/environment';
import { MsalService } from '@azure/msal-angular';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit {
  orders: any[] = [];
  products: any[] = [];
  cargando = true;
  error = '';
  isAdmin = false;
  isOperador = false;
  isCliente = false;

  mostrarFormulario = false;
  itemsSeleccionados: { productId: number; quantity: number }[] = [];

  // Transiciones de estado del flujo de pedidos
  estadosSiguientes: { [key: string]: string[] } = {
    'CREADO': ['ACEPTADO', 'CANCELADO'],
    'ACEPTADO': ['EN_PREPARACION', 'CANCELADO'],
    'EN_PREPARACION': ['DESPACHADO'],
    'DESPACHADO': ['ENTREGADO'],
    'ENTREGADO': [],
    'CANCELADO': []
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

    this.cargarPedidos();
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

  async cargarPedidos(): Promise<void> {
    this.cargando = true;
    this.error = '';
    try {
      const headers = await this.getHeaders();
      this.http.get<any[]>(`${environment.apiUrl}/api/orders`, { headers }).subscribe({
        next: (datos) => {
          this.orders = datos;
          this.cargando = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.error = 'No se pudieron consultar los pedidos.';
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

  async cargarProductos(): Promise<void> {
    try {
      const headers = await this.getHeaders();
      this.http.get<any[]>(`${environment.apiUrl}/api/catalog/products`, { headers }).subscribe({
        next: (datos) => { this.products = datos; },
        error: () => {}
      });
    } catch {}
  }

  abrirFormularioNuevo(): void {
    const primerProductoId = this.products.length > 0 ? this.products[0].id : null;
    this.itemsSeleccionados = [{ productId: primerProductoId, quantity: 1 }];
    this.mostrarFormulario = true;
  }

  agregarItem(): void {
    const primerProductoId = this.products.length > 0 ? this.products[0].id : null;
    this.itemsSeleccionados.push({ productId: primerProductoId, quantity: 1 });
  }

  eliminarItem(index: number): void {
    this.itemsSeleccionados.splice(index, 1);
  }

  cancelarFormulario(): void {
    this.mostrarFormulario = false;
  }

  async crearPedido(): Promise<void> {
    try {
      const headers = await this.getHeaders();
      // Mapear asegurando que los IDs y cantidades sean valores numéricos estrictos
      const payload = {
        items: this.itemsSeleccionados.map(item => ({
          productId: Number(item.productId),
          quantity: Number(item.quantity)
        }))
      };

      this.http.post(`${environment.apiUrl}/api/orders`, payload, { headers }).subscribe({
        next: () => {
          this.mostrarFormulario = false;
          this.cargarPedidos();
        },
        error: () => { this.error = 'Error al generar la orden.'; }
      });
    } catch {
      this.error = 'Error de autenticación.';
    }
  }

  getSiguientesEstados(estadoActual: string): string[] {
    return this.estadosSiguientes[estadoActual] || [];
  }

  async cambiarEstado(orderId: number, nuevoEstado: string): Promise<void> {
    try {
      const headers = await this.getHeaders();
      this.http.put(`${environment.apiUrl}/api/orders/${orderId}/status`, { status: nuevoEstado }, { headers }).subscribe({
        next: () => { this.cargarPedidos(); },
        error: (err) => { this.error = err.error?.message || 'No se pudo actualizar el estado.'; }
      });
    } catch {
      this.error = 'Error de autenticación.';
    }
  }

  getEstadoClass(estado: string): string {
    const clases: { [key: string]: string } = {
      'CREADO': 'badge-creado',
      'ACEPTADO': 'badge-aceptado',
      'EN_PREPARACION': 'badge-preparacion',
      'DESPACHADO': 'badge-despachado',
      'ENTREGADO': 'badge-entregado',
      'CANCELADO': 'badge-cancelado'
    };
    return clases[estado] || 'badge-default';
  }
}
