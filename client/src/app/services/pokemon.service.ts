import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PokemonService {
  constructor(private http: HttpClient) {}

  searchCards(params: any): Observable<{ cards: any[], totalPages: number }> {
    return this.http.get<{ cards: any[], totalPages: number }>('/api/cards/search', { params });
  }

  getCardDetails(cardId: string): Observable<any> {
    return this.http.get(`/api/cards/${cardId}`);
  }

  // New method to get all filters in one go
  getFilters(): Observable<{ sets: any[], types: string[], rarities: string[] }> {
    return this.http.get<{ sets: any[], types: string[], rarities: string[] }>('/api/cards/filters');
  }

  getFavorites(): Observable<{ data: any[] }> {
    return this.http.get<{ data: any[] }>('/api/favorites');
  }

  addToFavorites(cardId: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/favorites/add', { cardId });
  }

  removeFromFavorites(cardId: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/favorites/remove', { cardId });
  }

  getAllCards(): Observable<any[]> {
    return this.http.get<any[]>('/api/cards/raw');
  }

  createListing(frontImage: File, backImage: File, startingPrice: number, buyoutPrice: number, listingType: string, auctionStart?: string):
                 Observable<{ message: string, listingId: number }> {
    const formData = new FormData();
    formData.append('frontImage', frontImage);
    formData.append('backImage', backImage);
    formData.append('listingType', listingType);
    if (startingPrice) formData.append('startingPrice', startingPrice.toString());
    if (buyoutPrice) formData.append('buyoutPrice', buyoutPrice.toString());
    if (auctionStart) formData.append('auctionStart', auctionStart); // ISO string, e.g., "2025-03-15T10:00:00"
    return this.http.post<{ message: string, listingId: number }>('/api/marketplace/list', formData);
  }

  getActiveListings(): Observable<Listing[]> {
    return this.http.get<Listing[]>('/api/marketplace/active');
  }

  placeBid(listingId: number, bidAmount: number): Observable<{ message: string, bidId: number, bidAmount: number }> {
    return this.http.post<{ message: string, bidId: number, bidAmount: number }>('/api/marketplace/bid', { listingId, bidAmount });
  }

  createAuctionBuyoutIntent(listingId: number): Observable<{ clientSecret: string }> {
    return this.http.post<{ clientSecret: string }>('/api/marketplace/auction/buyout-intent', { listingId });
  }

  completeAuctionBuyout(listingId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/marketplace/auction/buyout', { listingId });
  }

  createPaymentIntent(listingId: number, amount: number): Observable<{ clientSecret: string }> {
    return this.http.post<{ clientSecret: string }>('/api/marketplace/payment-intent', { listingId, amount });
  }

  completePurchase(listingId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/marketplace/purchase', { listingId });
  }

  generateTelegramSubscriptionDeepLink(): Observable<{ deepLink: string, subscribed: string }> {
    return this.http.get<{ deepLink: string, subscribed: string }>('/api/telegram/subscribe');
  }
}

export interface Listing {
  id: number;
  userId: number;
  cardId: string;
  overallGrade: number;
  startingPrice: number;
  buyoutPrice: number;
  listingType: string;
  auctionStart?: string;
  auctionEnd?: string;
  status: string;
  frontImage: string; // Base64 string
  backImage: string; // Base64 string
}