import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PokemonService, Listing } from '../../services/pokemon.service';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-auction-listings',
  standalone: false,
  templateUrl: './auction-listings.component.html',
  styleUrls: ['./auction-listings.component.css']
})
export class AuctionListingsComponent implements OnInit {
  activeDataSource = new MatTableDataSource<Listing>();
  soldDataSource = new MatTableDataSource<Listing>();
  displayedColumns: string[] = ['id', 'cardName', 'cardSet', 'cardNumber', 'overallGrade', 'price', 'buyoutPrice', 'soldPrice', 'soldDate', 'frontImage', 'backImage', 'auctionEnd', 'actions'];
  displayedSoldColumns: string[] = ['id', 'cardName', 'cardSet', 'cardNumber', 'overallGrade', 'price', 'soldPrice', 'soldDate'];
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
            // Optionally open the spreadsheet
            // window.open(this.spreadsheetUrl, '_blank');
          } else {
            this.successMessage = params['message'] || 'Export successful, but no sold listings found.';
          }
        } else if (params['export'] === 'error') {
          this.error = params['reason'] || 'An unknown error occurred during export.';
          console.error('Export error:', this.error);
        }
      }
    });
    this.loadListings(); // Your existing method
    this.checkPendingExport(); // Your existing method
  }

  loadListings() {
    this.pokemonService.getActiveListings().subscribe({
      next: (listings) => {
        this.activeDataSource.data = listings.filter(l => l.listingType === 'AUCTION');
      },
      error: (err) => {
        this.error = 'Failed to load active listings: ' + err.message;
      }
    });

    this.pokemonService.getSoldListings().subscribe({
      next: (listings) => {
        this.soldDataSource.data = listings.filter(l => l.listingType === 'AUCTION');
      },
      error: (err) => {
        this.error = 'Failed to load sold listings: ' + err.message;
      }
    });
  }

  bid(listingId: number) {
    this.router.navigate(['/marketplace/bid', listingId]);
  }

  buyout(listingId: number) {
    this.router.navigate(['/marketplace/auction-purchase', listingId]);
  }

  authorizeGoogle() {
    localStorage.setItem('pendingExport', 'true');
    window.location.href = 'https://localhost:8443/api/marketplace/oauth2/authorize';
}

  exportToSheets(): void {
    // Instead of an HTTP request, redirect the browser to the backend's OAuth2 authorize endpoint
    window.location.href = 'https://localhost:8443/api/marketplace/oauth2/authorize';
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
    if (localStorage.getItem('pendingExport') === 'true' && this.router.url.includes('/marketplace/auctions')) {
      localStorage.removeItem('pendingExport');
      this.performExport();
    }
  }

  createListing() {
    this.router.navigate(['/marketplace/create']);
  }
}