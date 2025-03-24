import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PokemonService } from '../../services/pokemon.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-create-listing',
  standalone: false,
  templateUrl: './create-listing.component.html',
  styleUrls: ['./create-listing.component.css']
})
export class CreateListingComponent implements OnInit {
  listingForm: FormGroup;
  frontImage: File | null = null;
  backImage: File | null = null;
  message: string | null = null;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private pokemonService: PokemonService,
    private router: Router
  ) {
    this.listingForm = this.fb.group({
      cardName: ['', Validators.required],
      cardSet: ['', Validators.required],
      cardNumber: ['', Validators.required],
      listingType: ['FIXED', Validators.required],
      startingPrice: [null],
      buyoutPrice: [null],
      auctionStart: [null]
    });
  }

  ngOnInit() {
    this.listingForm.get('listingType')?.valueChanges.subscribe(type => {
      const startingPriceControl = this.listingForm.get('startingPrice');
      const buyoutPriceControl = this.listingForm.get('buyoutPrice');
      const auctionStartControl = this.listingForm.get('auctionStart');

      if (type === 'AUCTION') {
        startingPriceControl?.setValidators([Validators.required, Validators.min(0.01)]);
        buyoutPriceControl?.clearValidators();
        buyoutPriceControl?.setValidators(Validators.min(0.01));
        auctionStartControl?.setValidators(Validators.required);
      } else {
        startingPriceControl?.clearValidators();
        buyoutPriceControl?.setValidators([Validators.required, Validators.min(0.01)]);
        auctionStartControl?.clearValidators();
      }

      startingPriceControl?.updateValueAndValidity();
      buyoutPriceControl?.updateValueAndValidity();
      auctionStartControl?.updateValueAndValidity();
    });

    this.listingForm.get('listingType')?.updateValueAndValidity();
  }

  onFrontImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.frontImage = input.files[0];
    }
  }

  onBackImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.backImage = input.files[0];
    }
  }

  createListing() {
    if (this.listingForm.invalid || !this.frontImage || !this.backImage) {
      this.error = 'Please fill all required fields and upload front/back images.';
      this.listingForm.markAllAsTouched();
      return;
    }

    this.error = null;
    this.message = null;

    const { cardName, cardSet, cardNumber, listingType, startingPrice, buyoutPrice, auctionStart } = this.listingForm.value;
    const startingPriceToSend = listingType === 'AUCTION' ? startingPrice : null;
    const buyoutPriceToSend = listingType === 'FIXED' ? buyoutPrice : (listingType === 'AUCTION' ? buyoutPrice : null);

    this.pokemonService.createListing(
      this.frontImage,
      this.backImage,
      startingPriceToSend,
      buyoutPriceToSend,
      listingType,
      auctionStart,
      cardName,
      cardSet,
      cardNumber
    ).subscribe({
      next: (response) => {
        this.message = `Listing created successfully! Listing ID: ${response.listingId}`;
        this.resetForm();
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to create listing.';
      }
    });
  }

  private resetForm() {
    this.listingForm.reset({ listingType: 'FIXED' });
    this.frontImage = null;
    this.backImage = null;
    const frontInput = document.getElementById('frontImageInput') as HTMLInputElement;
    const backInput = document.getElementById('backImageInput') as HTMLInputElement;
    if (frontInput) frontInput.value = '';
    if (backInput) backInput.value = '';
  }

  goBack() {
    this.router.navigate(['/marketplace']);
  }
}