import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { NavbarComponent } from '../navbar/navbar.component';
import { environment } from '../../../environments/environment';
import { MsalService } from '@azure/msal-angular';

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

  constructor(private http: HttpClient, private msalService: MsalService) {}

  ngOnInit(): void {
    this.cargarProductosReales();
  }

  async cargarProductosReales(): Promise<void> {
    this.cargando = true;
    this.error = '';

    try {
      const account = this.msalService.instance.getActiveAccount();
      if (!account) {
        this.error = 'No hay cuenta activa';
        this.cargando = false;
        return;
      }

      const tokenResponse = await this.msalService.instance.acquireTokenSilent({
        scopes: [`api://${environment.clientId}/access_as_user`],
        account: account
      });

      console.log('Token obtenido:', tokenResponse.accessToken.substring(0, 50));

      const headers = new HttpHeaders({
        'Authorization': `Bearer ${tokenResponse.accessToken}`
      });

      this.http.get<any[]>(`${environment.apiUrl}/api/catalog/products`, { headers }).subscribe({
        next: (datos) => {
          console.log('Productos:', datos);
          this.products = datos;
          this.cargando = false;
        },
        error: (err) => {
          console.error('Error:', err);
          this.error = 'No se pudieron cargar los productos.';
          this.cargando = false;
        }
      });
    } catch (err) {
      console.error('Error obteniendo token:', err);
      this.error = 'Error de autenticaciÃ³n.';
      this.cargando = false;
    }
  }
}