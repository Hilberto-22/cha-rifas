import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
type NumberStatus = 'available' | 'reserved' | 'confirmed';
type PaymentMethod = 'PIX' | 'DIAPER' | 'CARD';
interface RaffleNumber {
  number: number;
  status: NumberStatus;
}
interface Raffle {
  id: string;
  title: string;
  drawAt: string;
  numberPrice: number;
  firstPrize: number;
  secondPrize: number;
  pixAvailable: boolean;
  whatsappNumber: string;
}
interface Reservation {
  id: string;
  numbers: number[];
  expiresAt: string;
}
@Component({
  selector: 'app-raffle-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
})
export class RafflePage implements OnInit, OnDestroy {
  private readonly http = inject(HttpClient);
  private refreshTimer?: ReturnType<typeof setInterval>;
  raffle = signal<Raffle | null>(null);
  numbers = signal<RaffleNumber[]>([]);
  selected = signal<number[]>([]);
  step = signal<'numbers' | 'form' | 'success'>('numbers');
  loading = signal(true);
  submitting = signal(false);
  error = signal('');
  reservation = signal<Reservation | null>(null);
  form = { name: '', phone: '', paymentMethod: 'PIX' as PaymentMethod };
  total = computed(() => this.selected().length * (this.raffle()?.numberPrice ?? 0));
  ngOnInit(): void {
    this.loadRaffle();
    this.refreshTimer = setInterval(() => this.loadNumbers(), 15_000);
  }
  ngOnDestroy(): void {
    if (this.refreshTimer) clearInterval(this.refreshTimer);
  }
  loadRaffle(): void {
    this.http.get<Raffle>('/api/v1/raffles/active').subscribe({
      next: (raffle) => {
        this.raffle.set(raffle);
        this.loadNumbers();
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Não foi possível carregar a rifa.');
      },
    });
  }
  loadNumbers(): void {
    const raffle = this.raffle();
    if (!raffle) return;
    this.error.set('');
    this.http.get<RaffleNumber[]>(`/api/v1/raffles/${raffle.id}/numbers`).subscribe({
      next: (numbers) => {
        this.numbers.set(numbers);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Não foi possível carregar os números. Verifique se o backend está em execução.');
      },
    });
  }
  toggleNumber(item: RaffleNumber): void {
    if (item.status !== 'available') return;
    this.selected.update((current) =>
      current.includes(item.number)
        ? current.filter((number) => number !== item.number)
        : [...current, item.number].sort((a, b) => a - b),
    );
  }
  reserve(): void {
    const raffle = this.raffle();
    if (!raffle || !this.selected().length || this.submitting()) return;
    this.submitting.set(true);
    this.error.set('');
    this.http
      .post<Reservation>(`/api/v1/raffles/${raffle.id}/reservations`, {
        ...this.form,
        numbers: this.selected(),
      })
      .subscribe({
        next: (reservation) => {
          this.reservation.set(reservation);
          this.step.set('success');
          this.submitting.set(false);
          this.loadNumbers();
        },
        error: (response: HttpErrorResponse) => {
          const unavailable: number[] = response.error?.unavailable ?? [];
          if (response.status === 409) {
            this.selected.update((current) =>
              current.filter((number) => !unavailable.includes(number)),
            );
            this.loadNumbers();
          }
          this.error.set(response.error?.message ?? 'Não foi possível concluir a reserva.');
          this.submitting.set(false);
        },
      });
  }
  openWhatsApp(): void {
    const raffle = this.raffle();
    const reservation = this.reservation();
    if (!raffle || !reservation) return;
    const payment = { PIX: 'Pix', DIAPER: 'pacote de fraldas', CARD: 'cartão' }[
      this.form.paymentMethod
    ];
    const message = `Olá! Sou ${this.form.name}. Reservei os números ${reservation.numbers.join(', ')} na ${raffle.title}. Escolhi pagar com ${payment}. Código da reserva: ${reservation.id.slice(0, 8)}.`;
    window.location.href = `https://wa.me/${raffle.whatsappNumber}?text=${encodeURIComponent(message)}`;
  }
  money(value: number | undefined): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(
      value ?? 0,
    );
  }
}
