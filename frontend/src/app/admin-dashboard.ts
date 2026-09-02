import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
interface AdminReservation {
  id: string;
  participantName: string;
  phone: string;
  paymentMethod: string;
  status: string;
  numbers: number[];
  total: number;
  createdAt: string;
  expiresAt: string;
  paymentReportedAt: string | null;
}
@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  template: ` <main class="admin-page">
    <header>
      <div>
        <span class="kicker">CHÁ RIFA</span>
        <h1>Painel administrativo</h1>
      </div>
      <div class="actions">
        <button class="refresh" (click)="load()">Atualizar</button
        ><button class="logout" (click)="logout()">Sair</button>
      </div>
    </header>
    <section class="metrics">
      <article>
        <span>Reservas</span><strong>{{ reservations().length }}</strong>
      </article>
      <article>
        <span>Pendentes</span><strong>{{ pendingCount() }}</strong>
      </article>
      <article>
        <span>Pagamento informado</span><strong>{{ reportedCount() }}</strong>
      </article>
      <article>
        <span>Números reservados</span><strong>{{ reservedNumbers() }}</strong>
      </article>
      <article>
        <span>Valor pendente</span><strong>{{ money(pendingTotal()) }}</strong>
      </article>
    </section>
    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Participantes e reservas</h2>
          <p>Dados atualizados diretamente do PostgreSQL.</p>
        </div>
      </div>
      @if (loading()) {
        <div class="empty">Carregando reservas...</div>
      } @else if (!reservations().length) {
        <div class="empty">Nenhuma reserva foi realizada ainda.</div>
      } @else {
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Participante</th>
                <th>Contato</th>
                <th>Números</th>
                <th>Pagamento</th>
                <th>Situação</th>
                <th>Total</th>
                <th>Criada em</th>
                <th>Validade</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              @for (item of reservations(); track item.id) {
                <tr>
                  <td>
                    <strong>{{ item.participantName }}</strong
                    ><small>#{{ item.id.slice(0, 8) }}</small>
                  </td>
                  <td>{{ item.phone }}</td>
                  <td>
                    <div class="number-list">
                      @for (number of item.numbers; track number) {
                        <b>{{ number }}</b>
                      }
                    </div>
                  </td>
                  <td>{{ paymentLabel(item.paymentMethod) }}</td>
                  <td>
                    <span class="status" [class]="'status ' + item.status.toLowerCase()">{{
                      statusLabel(item.status)
                    }}</span>
                    @if (item.paymentReportedAt) {
                      <small>{{ item.paymentReportedAt | date: 'dd/MM/yy HH:mm' : '-0300' }}</small>
                    }
                  </td>
                  <td>
                    <strong>{{ money(item.total) }}</strong>
                  </td>
                  <td>{{ item.createdAt | date: 'dd/MM/yy HH:mm' : '-0300' }}</td>
                  <td>{{ item.expiresAt | date: 'dd/MM/yy HH:mm' : '-0300' }}</td>
                  <td>
                    <div class="row-actions">
                      @if (item.status === 'PENDING' || item.status === 'PAYMENT_REPORTED') {
                        <button class="confirm" (click)="changeStatus(item.id, 'confirm')">
                          Confirmar</button
                        ><button (click)="changeStatus(item.id, 'cancel')">Cancelar</button>
                      }
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </section>
  </main>`,
  styles: [
    `
      .admin-page {
        min-height: 100vh;
        padding: 38px clamp(20px, 5vw, 70px);
        background: #f3f5f4;
        color: #284657;
        font-family: 'DM Sans', sans-serif;
      }
      header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 30px;
      }
      .kicker {
        color: #7196b4;
        font-size: 11px;
        font-weight: 700;
        letter-spacing: 0.17em;
      }
      h1 {
        margin: 6px 0;
        font:
          600 clamp(30px, 4vw, 46px) 'Playfair Display',
          serif;
      }
      .actions {
        display: flex;
        gap: 8px;
      }
      .actions button,
      .row-actions button {
        padding: 10px 16px;
        border-radius: 6px;
        border: 1px solid #c9d4d9;
        background: white;
        color: #36566a;
        cursor: pointer;
      }
      .actions .logout {
        border-color: #27475d;
        background: #27475d;
        color: white;
      }
      .metrics {
        display: grid;
        grid-template-columns: repeat(5, 1fr);
        gap: 14px;
        margin-bottom: 24px;
      }
      .metrics article {
        padding: 22px;
        border: 1px solid #e0e5e4;
        border-radius: 10px;
        background: white;
      }
      .metrics span {
        display: block;
        color: #788b95;
        font-size: 12px;
      }
      .metrics strong {
        display: block;
        margin-top: 9px;
        font:
          600 27px 'Playfair Display',
          serif;
      }
      .panel {
        background: white;
        border: 1px solid #e0e5e4;
        border-radius: 12px;
        overflow: hidden;
      }
      .panel-head {
        padding: 24px;
        border-bottom: 1px solid #e6e9e8;
      }
      .panel h2 {
        margin: 0;
        font:
          600 23px 'Playfair Display',
          serif;
      }
      .panel p {
        margin: 5px 0 0;
        color: #84939a;
        font-size: 12px;
      }
      .table-wrap {
        overflow: auto;
      }
      table {
        width: 100%;
        border-collapse: collapse;
        font-size: 13px;
      }
      th {
        text-align: left;
        padding: 13px 16px;
        background: #f7f9f8;
        color: #74868f;
        font-size: 10px;
        text-transform: uppercase;
        letter-spacing: 0.08em;
      }
      td {
        padding: 16px;
        border-top: 1px solid #edf0ef;
        white-space: nowrap;
      }
      td small {
        display: block;
        margin-top: 4px;
        color: #9aa7ad;
      }
      .number-list,
      .row-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 4px;
        max-width: 210px;
      }
      .number-list b {
        display: grid;
        place-items: center;
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: #e7f0f5;
        color: #41677f;
        font-size: 11px;
      }
      .row-actions button {
        padding: 6px 9px;
        font-size: 10px;
      }
      .row-actions .confirm {
        border-color: #3d8261;
        background: #3d8261;
        color: white;
      }
      .status {
        padding: 5px 9px;
        border-radius: 20px;
        background: #eee;
        color: #666;
        font-size: 10px;
        font-weight: 700;
      }
      .status.pending {
        background: #fff0ce;
        color: #8a621a;
      }
      .status.confirmed {
        background: #dceee5;
        color: #347254;
      }
      .status.payment_reported {
        background: #dce8f4;
        color: #315f77;
      }
      .status.expired,
      .status.cancelled {
        background: #f1dddd;
        color: #914747;
      }
      .empty {
        padding: 60px;
        text-align: center;
        color: #85949b;
      }
      @media (max-width: 850px) {
        header {
          align-items: flex-start;
          gap: 15px;
        }
        .metrics {
          grid-template-columns: repeat(2, 1fr);
        }
        .admin-page {
          padding: 25px 15px;
        }
      }
      @media (max-width: 500px) {
        header {
          flex-direction: column;
        }
        .metrics {
          grid-template-columns: 1fr 1fr;
        }
        .metrics article {
          padding: 16px;
        }
        .metrics strong {
          font-size: 21px;
        }
      }
    `,
    `
      @media (max-width: 720px) {
        .admin-page {
          padding: 20px 12px;
        }
        header {
          align-items: stretch;
        }
        .actions button {
          flex: 1;
          min-height: 44px;
        }
        .metrics {
          gap: 8px;
        }
        .metrics article {
          padding: 14px;
        }
        .panel-head {
          padding: 18px;
        }
        .table-wrap {
          overflow: visible;
        }
        table,
        tbody,
        tr,
        td {
          display: block;
          width: 100%;
        }
        thead {
          display: none;
        }
        tbody {
          display: grid;
          gap: 12px;
          padding: 12px;
        }
        tr {
          border: 1px solid #e2e7e6;
          border-radius: 9px;
          padding: 8px;
          background: #fff;
          box-shadow: 0 5px 16px #2947580d;
        }
        td {
          display: grid;
          grid-template-columns: 105px minmax(0, 1fr);
          align-items: center;
          gap: 8px;
          padding: 8px;
          border: 0;
          white-space: normal;
          overflow-wrap: anywhere;
        }
        td:before {
          color: #82929a;
          font-size: 9px;
          font-weight: 700;
          letter-spacing: 0.07em;
          text-transform: uppercase;
        }
        td:nth-child(1):before {
          content: 'Participante';
        }
        td:nth-child(2):before {
          content: 'Contato';
        }
        td:nth-child(3):before {
          content: 'Números';
        }
        td:nth-child(4):before {
          content: 'Pagamento';
        }
        td:nth-child(5):before {
          content: 'Situação';
        }
        td:nth-child(6):before {
          content: 'Total';
        }
        td:nth-child(7):before {
          content: 'Criada em';
        }
        td:nth-child(8):before {
          content: 'Validade';
        }
        td:nth-child(9):before {
          content: 'Ações';
        }
        .number-list,
        .row-actions {
          max-width: none;
        }
        .row-actions button {
          min-height: 40px;
          flex: 1 1 90px;
        }
        .empty {
          padding: 35px 15px;
        }
      }
      @media (max-width: 380px) {
        .metrics {
          grid-template-columns: 1fr;
        }
        td {
          grid-template-columns: 88px minmax(0, 1fr);
        }
      }
    `,
  ],
})
export class AdminDashboard implements OnInit {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private router = inject(Router);
  reservations = signal<AdminReservation[]>([]);
  loading = signal(true);
  pendingCount = computed(() => this.reservations().filter((r) => r.status === 'PENDING').length);
  reportedCount = computed(
    () => this.reservations().filter((r) => r.status === 'PAYMENT_REPORTED').length,
  );
  reservedNumbers = computed(() =>
    this.reservations()
      .filter(
        (r) =>
          r.status === 'PENDING' || r.status === 'PAYMENT_REPORTED' || r.status === 'CONFIRMED',
      )
      .reduce((sum, r) => sum + r.numbers.length, 0),
  );
  pendingTotal = computed(() =>
    this.reservations()
      .filter((r) => r.status === 'PENDING' || r.status === 'PAYMENT_REPORTED')
      .reduce((sum, r) => sum + r.total, 0),
  );
  ngOnInit(): void {
    this.load();
  }
  load(): void {
    this.loading.set(true);
    this.http.get<AdminReservation[]>('/api/v1/admin/reservations').subscribe({
      next: (data) => {
        this.reservations.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
  logout(): void {
    this.auth.logout();
    this.router.navigate(['/admin/login']);
  }
  money(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  }
  changeStatus(id: string, action: 'confirm' | 'cancel'): void {
    this.http
      .patch<void>(`/api/v1/admin/reservations/${id}/${action}`, {})
      .subscribe(() => this.load());
  }
  paymentLabel(value: string): string {
    return { PIX: 'Pix', DIAPER: 'Fralda', CARD: 'Cartão' }[value] ?? value;
  }
  statusLabel(value: string): string {
    return (
      {
        PENDING: 'Pendente',
        PAYMENT_REPORTED: 'Pagamento informado',
        CONFIRMED: 'Confirmada',
        EXPIRED: 'Expirada',
        CANCELLED: 'Cancelada',
      }[value] ?? value
    );
  }
}
