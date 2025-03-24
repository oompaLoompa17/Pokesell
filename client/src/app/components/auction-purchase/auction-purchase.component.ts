import { Component, OnInit, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PokemonService, Listing } from '../../services/pokemon.service';
import { loadStripe, Stripe, StripeElements, StripeCardElement } from '@stripe/stripe-js';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-auction-purchase',
  standalone: false,
  templateUrl: './auction-purchase.component.html',
  styleUrls: ['./auction-purchase.component.css']
})
export class AuctionPurchaseComponent implements OnInit, AfterViewInit, OnDestroy {
  listing: Listing | null = null;
  error: string | null = null;
  message: string | null = null;
  loading = true;
  processing = false;
  stripe: Stripe | null = null;
  elements: StripeElements | null = null;
  card: StripeCardElement | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pokemonService: PokemonService,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    await this.initializeStripe();
    this.loadListing();
  }

  ngAfterViewInit() {}

  ngOnDestroy() {
    if (this.card) {
      this.card.unmount();
    }
  }

  async initializeStripe() {
    try {
      this.stripe = await loadStripe('pk_test_51R20nGJ0Tn7Ij89xL6M6udOEhJ77CWu4TEBBReFPlEBTIUGxvrWhMZIH7VxCIpdDzdk3qFGFG95ntf1NZ7e18Tsr00mKX3QgXA');
    } catch (err) {
      this.error = 'Failed to initialize payment form. Please try again later.';
    }
  }

  private initializeStripeElements() {
    if (this.stripe && !this.elements) {
      this.elements = this.stripe.elements();
      this.card = this.elements.create('card', {
        style: {
          base: {
            fontSize: '16px',
            color: '#32325d',
            fontFamily: 'Roboto, sans-serif',
            '::placeholder': { color: '#aab7c4' },
          },
          invalid: { color: '#fa755a' },
        },
      });
      const cardElement = document.getElementById('card-element');
      if (cardElement) {
        this.card.mount('#card-element');
      } else {
        this.error = 'Payment form failed to load. Please refresh the page.';
      }
    }
  }

  loadListing() {
    const listingId = Number(this.route.snapshot.paramMap.get('listingId'));
    this.pokemonService.getActiveListings().subscribe({
      next: (listings) => {
        this.listing = listings.find(l => l.id === listingId && l.listingType === 'AUCTION' && l.buyoutPrice) || null;
        if (!this.listing) {
          this.error = 'Listing not found or not available for buyout.';
        }
        this.loading = false;
        this.cdr.detectChanges();
        if (this.listing && !this.loading) {
          setTimeout(() => this.initializeStripeElements(), 0);
        }
      },
      error: (err) => {
        this.error = 'Failed to load listing: ' + err.message;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  async pay() {
    if (!this.stripe || !this.card || !this.listing) {
      this.error = 'Payment form is not ready. Please refresh the page.';
      return;
    }

    this.processing = true;
    try {
      const response = await firstValueFrom(this.pokemonService.createAuctionBuyoutIntent(this.listing.id));
      if (!response?.clientSecret) {
        throw new Error('Failed to retrieve payment intent client secret');
      }
      const clientSecret = response.clientSecret;

      const result = await this.stripe.confirmCardPayment(clientSecret, {
        payment_method: { card: this.card }
      });

      if (result.error) {
        this.error = result.error.message || 'Payment failed.';
      } else if (result.paymentIntent?.status === 'succeeded') {
        await firstValueFrom(this.pokemonService.completeAuctionBuyout(this.listing.id));
        this.message = 'Buyout successful! Listing purchased.';
        this.error = null;
        this.listing.soldPrice = this.listing.buyoutPrice;
        this.listing.soldDate = new Date().toISOString();
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/marketplace/auctions']), 2000);
      }
    } catch (err) {
      this.error = 'An error occurred during payment: ' + (err instanceof Error ? err.message : 'Unknown error');
    } finally {
      this.processing = false;
    }
  }

  goBack() {
    this.router.navigate(['/marketplace/auctions']);
  }
}