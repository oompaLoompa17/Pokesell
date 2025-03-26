import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PokemonService, Listing } from '../../services/pokemon.service';
import { MatTableDataSource } from '@angular/material/table';
import { interval, Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-auction-listings',
  standalone: false,
  templateUrl: './auction-listings.component.html',
  styleUrls: ['./auction-listings.component.css']
})
export class AuctionListingsComponent implements OnInit, OnDestroy {
  activeDataSource = new MatTableDataSource<Listing>();
  soldDataSource = new MatTableDataSource<Listing>();
  displayedColumns: string[] = ['id', 'cardName', 'cardSet', 'cardNumber', 'overallGrade', 'price', 'buyoutPrice', 'frontImage', 'backImage', 'auctionEnd', 'actions'];
  displayedSoldColumns: string[] = ['id', 'cardName', 'cardSet', 'cardNumber', 'overallGrade', 'soldPrice', 'soldDate'];
  error: string | null = null;
  successMessage: string | null = null;
  spreadsheetUrl: string | null = null;  
  private timerSubscription!: Subscription;

  constructor(private pokemonService: PokemonService, private authService: AuthService, private router: Router, private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['export']) {
        if (params['export'] === 'success') {
          if (params['spreadsheetUrl']) {
            this.successMessage = 'Export successful!';
            this.spreadsheetUrl = params['spreadsheetUrl'];
            console.log('Spreadsheet URL:', this.spreadsheetUrl);
            // window.open(this.spreadsheetUrl, '_blank'); // Optionally open the spreadsheet
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
    // Start the real-time timer
    this.timerSubscription = interval(1000).subscribe(() => {
      // Trigger change detection by reassigning the data array
      this.activeDataSource.data = [...this.activeDataSource.data];
    });
  }

  createListing() {this.router.navigate(['/create-listing']);}

  loadListings() {
    this.pokemonService.getActiveListings().subscribe({
      next: (listings) => {this.activeDataSource.data = listings.filter(l => l.listingType === 'AUCTION');},
      error: (err) => {this.error = 'Failed to load active listings: ' + err.message;}});
    this.pokemonService.getSoldListings().subscribe({
      next: (listings) => {this.soldDataSource.data = listings.filter(l => l.listingType === 'AUCTION');},
      error: (err) => {this.error = 'Failed to load sold listings: ' + err.message;}});
  }

  placeBid(listingId: number, bidAmount: string, listing: Listing) {
    const bidValue = parseFloat(bidAmount);
    
    if (!bidValue || (listing.startingPrice !== undefined && bidValue < listing.startingPrice) || 
        (listing.buyoutPrice && bidValue > listing.buyoutPrice)) {
      this.error = 'Invalid bid amount';
      return;
    }

    this.pokemonService.placeBid(listingId, bidValue).subscribe({
      next: (response) => {
        this.error = null;
        this.successMessage = `Bid of ${bidValue} SGD placed successfully!`;
        listing.startingPrice = response.bidAmount; // Update current bid
        this.loadListings(); // Refresh listings
      },
      error: (err) => {this.error = 'Failed to place bid: ' + err.message;}});
  }

  getTimeLeft(auctionEnd: string): string {
    const endTime = new Date(auctionEnd).getTime();
    const now = new Date().getTime();
    const diff = endTime - now;

    if (diff <= 0) return 'Ended';
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);
    return `${days}d ${hours}h ${minutes}m ${seconds}s`;
  }

  buyout(listingId: number) {this.router.navigate(['/marketplace/auction-purchase', listingId]);}

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
    if (localStorage.getItem('pendingExport') === 'true' && this.router.url.includes('/marketplace/auctions')) {
      localStorage.removeItem('pendingExport');
      this.performExport();
    }
  }

  ngOnDestroy() {
    // Clean up the subscription to prevent memory leaks
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }
}