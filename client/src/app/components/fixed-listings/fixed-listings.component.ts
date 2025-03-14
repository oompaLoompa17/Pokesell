import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Listing, PokemonService } from '../../services/pokemon.service';

@Component({
  selector: 'app-fixed-listings',
  standalone: false,
  templateUrl: './fixed-listings.component.html',
  styleUrls: ['./fixed-listings.component.css']
})
export class FixedListingsComponent implements OnInit {
  listings: Listing[] = [];
  error: string | null = null;
  displayedColumns: string[] = ['id', 'overallGrade', 'price', 'frontImage', 'actions'];

  constructor(private pokemonService: PokemonService, private router: Router) {}

  ngOnInit() {
    this.loadFixedListings();
  }

  loadFixedListings() {
    this.pokemonService.getActiveListings().subscribe({
      next: (listings) => {
        this.listings = listings.filter(listing => listing.listingType === 'FIXED');
      },
      error: (err) => {
        this.error = 'Failed to load fixed-price listings: ' + err.message;
      }
    });
  }

  goBack() {
    this.router.navigate(['/marketplace']);
  }
}