import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PokemonService, Listing } from '../../services/pokemon.service';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-fixed-listings',
  standalone: false,
  templateUrl: './fixed-listings.component.html',
  styleUrls: ['./fixed-listings.component.css']
})
export class FixedListingsComponent implements OnInit {
  activeDataSource = new MatTableDataSource<Listing>();
  soldDataSource = new MatTableDataSource<Listing>();
  displayedColumns: string[] = ['id', 'cardName', 'cardSet', 'cardNumber', 'overallGrade', 'price', 'frontImage', 'backImage', 'actions'];
  displayedSoldColumns: string[] = ['id', 'cardName', 'cardSet', 'cardNumber', 'overallGrade', 'soldPrice', 'soldDate'];
  error: string | null = null;
  successMessage: string | null = null;
  spreadsheetUrl: string | null = null;

  constructor(private pokemonService: PokemonService, private router: Router, private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['export']) {
        if (params['export'] === 'success') {
          if (params['spreadsheetUrl']) {
            this.successMessage = 'Export successful!';
            this.spreadsheetUrl = params['spreadsheetUrl'];
            console.log('Spreadsheet URL:', this.spreadsheetUrl);
          } else {
            this.successMessage = params['message'] || 'Export successful, but no sold listings found.';
          }
        } else if (params['export'] === 'error') {
          this.error = params['reason'] || 'An unknown error occurred during export.';
          console.error('Export error:', this.error);
        }
      }
    });
    this.loadListings();
    this.checkPendingExport();
  }

  loadListings() {
    this.pokemonService.getActiveListings().subscribe({
      next: (listings) => {
        this.activeDataSource.data = listings.filter(l => l.listingType === 'FIXED');
      },
      error: (err) => {
        this.error = 'Failed to load active listings: ' + err.message;
      }
    });

    this.pokemonService.getSoldListings().subscribe({
      next: (listings) => {
        this.soldDataSource.data = listings.filter(l => l.listingType === 'FIXED');
      },
      error: (err) => {
        this.error = 'Failed to load sold listings: ' + err.message;
      }
    });
  }

  purchase(listingId: number) {
    this.router.navigate(['/marketplace/purchase', listingId]);
  }

  authorizeGoogle() {
    localStorage.setItem('pendingExport', 'true');
    window.location.href = '/api/marketplace/oauth2/authorize';
  }

  exportToSheets(): void {
    window.location.href = '/api/marketplace/oauth2/authorize';
  }

  private performExport() {
    this.pokemonService.exportToGoogleSheets().subscribe({
      next: (response) => {
        alert(response.message);
        if (response.spreadsheetUrl) {
          window.open(response.spreadsheetUrl, '_blank');
        }
      },
      error: (err) => {
        if (err.status === 401) {
          alert('Google OAuth required. Redirecting to authorize...');
          this.authorizeGoogle();
        } else {
          alert('Failed to export: ' + (err.error?.message || err.message));
        }
      }
    });
  }

  private checkPendingExport() {
    if (localStorage.getItem('pendingExport') === 'true' && this.router.url.includes('/marketplace/fixed')) {
      localStorage.removeItem('pendingExport');
      this.performExport();
    }
  }

  exportToGoogleSheets() {
    const soldListings = this.soldDataSource.data;
    if (soldListings.length === 0) {
      alert('No sold listings to export.');
      return;
    }

    const headers = ['ID', 'Card Name', 'Set', 'Card Number', 'Grade', 'Price', 'Sold Price', 'Sold Date'];
    const rows = soldListings.map(listing => [
      listing.id,
      listing.cardName,
      listing.cardSet,
      listing.cardNumber,
      listing.overallGrade,
      listing.buyoutPrice,
      listing.soldPrice || '',
      listing.soldDate ? new Date(listing.soldDate).toLocaleString() : ''
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'sold_fixed_listings.csv';
    a.click();
    window.URL.revokeObjectURL(url);

    // Open Google Sheets with a pre-filled URL (user will need to paste CSV manually)
    const googleSheetsUrl = 'https://docs.google.com/spreadsheets/create';
    window.open(googleSheetsUrl, '_blank');
  }

  createListing() {this.router.navigate(['/create-listing']);}
}