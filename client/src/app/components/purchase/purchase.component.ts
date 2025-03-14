import { Component, OnInit, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PokemonService, Listing } from '../../services/pokemon.service';
import { loadStripe, Stripe, StripeElements, StripeCardElement } from '@stripe/stripe-js';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-purchase',
  standalone: false,
  templateUrl: './purchase.component.html',
  styleUrls: ['./purchase.component.css']
})
export class PurchaseComponent implements OnInit, AfterViewInit, OnDestroy {
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
    private cdr: ChangeDetectorRef // Inject ChangeDetectorRef to manually trigger change detection
  ) {}

  async ngOnInit() {
    await this.initializeStripe();
    this.loadListing();
  }

  ngAfterViewInit() {
    // No need to initialize here; we'll handle it after loading the listing
  }

  ngOnDestroy() {
    if (this.card) {
      this.card.unmount();
      console.log('Stripe card element unmounted');
    }
  }

  async initializeStripe() {
    try {
      this.stripe = await loadStripe('pk_test_51R20nGJ0Tn7Ij89xL6M6udOEhJ77CWu4TEBBReFPlEBTIUGxvrWhMZIH7VxCIpdDzdk3qFGFG95ntf1NZ7e18Tsr00mKX3QgXA');
      if (this.stripe) {
        console.log('Stripe loaded successfully');
      } else {
        throw new Error('Stripe initialization failed');
      }
    } catch (err) {
      console.error('Error loading Stripe:', err);
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
            '::placeholder': {
              color: '#aab7c4',
            },
          },
          invalid: {
            color: '#fa755a',
          },
        },
      });
      const cardElement = document.getElementById('card-element');
      if (cardElement) {
        console.log('Mounting card element to #card-element');
        this.card.mount('#card-element');
        this.card.on('ready', () => {
          console.log('Card element is ready for input');
        });
      } else {
        console.error('Card element not found in DOM');
        this.error = 'Payment form failed to load. Please refresh the page.';
      }
    } else if (!this.stripe) {
      console.error('Stripe not initialized');
      this.error = 'Payment form initialization failed. Please check your network.';
    }
  }

  loadListing() {
    const listingId = Number(this.route.snapshot.paramMap.get('listingId'));
    this.pokemonService.getActiveListings().subscribe({
      next: (listings) => {
        this.listing = listings.find(l => l.id === listingId && l.listingType === 'FIXED') || null;
        if (!this.listing) {
          this.error = 'Listing not found or not available for purchase.';
        }
        this.loading = false;
        this.cdr.detectChanges(); // Manually trigger change detection to ensure DOM updates
        if (this.listing && !this.loading) {
          // Delay initialization to ensure DOM is ready
          setTimeout(() => {
            this.initializeStripeElements();
          }, 0);
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
      this.processing = false;
      return;
    }

    this.processing = true;
    try {
      console.log('Creating payment intent for listing:', this.listing.id);
      const response = await firstValueFrom(
        this.pokemonService.createPaymentIntent(this.listing.id, this.listing.buyoutPrice)
      );
      if (!response?.clientSecret) {
        throw new Error('Failed to retrieve payment intent client secret');
      }
      const clientSecret = response.clientSecret;
      console.log('Payment intent created, clientSecret:', clientSecret);

      console.log('Confirming card payment');
      const result = await this.stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: this.card
        }
      });

      if (result.error) {
        this.error = result.error.message || 'Payment failed.';
        console.error('Payment error:', result.error);
      } else if (result.paymentIntent?.status === 'succeeded') {
        console.log('Payment succeeded, completing purchase');
        await firstValueFrom(this.pokemonService.completePurchase(this.listing.id));
        this.message = 'Payment successful! Listing purchased.';
        this.error = null;
        setTimeout(() => this.router.navigate(['/marketplace/fixed']), 2000);
      }
    } catch (err) {
      this.error = 'An error occurred during payment: ' + (err instanceof Error ? err.message : 'Unknown error');
      console.error('Payment error:', err);
    } finally {
      this.processing = false;
    }
  }

  goBack() {
    this.router.navigate(['/marketplace/fixed']);
  }
}