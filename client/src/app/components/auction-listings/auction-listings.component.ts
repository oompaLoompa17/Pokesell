import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Listing, PokemonService } from '../../services/pokemon.service';
import { interval, Subscription } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-auction-listings',
  standalone: false,
  templateUrl: './auction-listings.component.html',
  styleUrls: ['./auction-listings.component.css']
})
export class AuctionListingsComponent implements OnInit, OnDestroy {
  listings: Listing[] = [];
  error: string | null = null;
  displayedColumns: string[] = ['id', 'overallGrade', 'price', 'buyoutPrice', 'frontImage', 'backImage', 'auctionEnd', 'actions'];
  private timerSubscription!: Subscription;
  bidForms: { [key: number]: FormGroup } = {};
  telegramDeepLink: string | null = null;
  isTelegramSubscribed: boolean = false;

  constructor(
    private pokemonService: PokemonService,
    private router: Router,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadAuctionListings();
    this.checkTelegramSubscription();
    this.timerSubscription = interval(1000).subscribe(() => this.updateTimers());
  }

  ngOnDestroy() {
    this.timerSubscription.unsubscribe();
  }

  loadAuctionListings() {
    this.pokemonService.getActiveListings().subscribe({
      next: (listings) => {
        this.listings = listings.filter(listing => listing.listingType === 'AUCTION');
        this.listings.forEach(listing => {
          this.bidForms[listing.id] = this.fb.group({
            bidAmount: ['', [Validators.required, Validators.min(listing.startingPrice + 0.01)]]
          });
        });
      },
      error: (err) => {
        this.error = 'Failed to load auction listings: ' + err.message;
      }
    });
  }

  getTimeLeft(auctionEnd?: string): string {
    if (!auctionEnd) return 'N/A';
    const end = new Date(auctionEnd).getTime();
    const now = new Date().getTime();
    const diff = end - now;
    if (diff <= 0) return 'Ended';
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);
    return `${hours}h ${minutes}m ${seconds}s`;
  }

  updateTimers() {
    this.listings = [...this.listings]; // Trigger change detection
  }

  placeBid(listingId: number) {
    const form = this.bidForms[listingId];
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    const bidAmount = form.get('bidAmount')?.value;
    this.pokemonService.placeBid(listingId, bidAmount).subscribe({
      next: (response) => {
        this.snackBar.open(`Bid of SGD ${response.bidAmount} placed on listing ${listingId}`, 'Close', { duration: 3000 });
        this.loadAuctionListings(); // Refresh listings to show updated highest bid
      },
      error: (err) => {
        this.snackBar.open(err.error?.error || 'Failed to place bid', 'Close', { duration: 3000 });
      }
    });
  }

  buyNow(listingId: number) {
    this.router.navigate(['/marketplace/auction-purchase', listingId]);
  }

  goBack() {
    this.router.navigate(['/marketplace']);
  }

  checkTelegramSubscription() {
    this.pokemonService.generateTelegramSubscriptionDeepLink().subscribe({
      next: (response) => {
        this.telegramDeepLink = response.deepLink || null;
        this.isTelegramSubscribed = response.subscribed === 'true';
      },
      error: (err) => {
        this.error = 'Failed to check Telegram subscription: ' + err.message;
      }
    });
  }

  connectTelegram() {
    if (this.telegramDeepLink) {
      window.open(this.telegramDeepLink, '_blank');
      setTimeout(() => this.checkTelegramSubscription(), 5000); // Re-check after 5s to update UI
    }
  }
}