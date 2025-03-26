import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PokemonService {
  constructor(private http: HttpClient, private authService: AuthService) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();}

  searchCards(params: any): Observable<{ cards: any[], totalPages: number }> {
    return this.http.get<{ cards: any[], totalPages: number }>('/api/cards/search', { params });}

  getCardDetails(cardId: string): Observable<any> {
    return this.http.get(`/api/cards/${cardId}`);}

  getFilters(): Observable<{ sets: any[], types: string[], rarities: string[] }> {
    return this.http.get<{ sets: any[], types: string[], rarities: string[] }>('/api/cards/filters');}

  getFavorites(): Observable<{ data: any[] }> {return this.http.get<{ data: any[] }>('/api/favorites');}

  addToFavorites(cardId: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/favorites/add', { cardId });}

  removeFromFavorites(cardId: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/favorites/remove', { cardId });}

  getAllCards(): Observable<any[]> {return this.http.get<any[]>('/api/cards/raw');}

  createListing(
    frontImage: File,
    backImage: File,
    startingPrice: number | null,
    buyoutPrice: number | null,
    listingType: string,
    auctionStart?: string,
    cardName?: string,
    cardSet?: string,
    cardNumber?: string
  ): Observable<{ message: string, listingId: number }> {
    const formData = new FormData();
    formData.append('frontImage', frontImage);
    formData.append('backImage', backImage);
    formData.append('listingType', listingType);
    if (cardName) formData.append('cardName', cardName);
    if (cardSet) formData.append('cardSet', cardSet);
    if (cardNumber) formData.append('cardNumber', cardNumber);
    if (startingPrice) formData.append('startingPrice', startingPrice.toString());
    if (buyoutPrice) formData.append('buyoutPrice', buyoutPrice.toString());
    if (auctionStart) formData.append('auctionStart', auctionStart);
    return this.http.post<{ message: string, listingId: number }>('/api/marketplace/list', formData);
  }
  
  getActiveListings(): Observable<Listing[]> {
    return this.http.get<any[]>('/api/marketplace/active', { headers: this.getAuthHeaders() }).pipe(
      map(listings => listings.map(listing => ({
        id: listing.id,
        userId: listing.userId,
        cardName: listing.cardName,
        cardSet: listing.cardSet,
        cardNumber: listing.cardNumber,
        overallGrade: listing.overallGrade,
        startingPrice: listing.startingPrice,
        buyoutPrice: listing.buyoutPrice,
        soldPrice: listing.soldPrice,
        soldDate: listing.soldDate,
        listingType: listing.listingType,
        auctionStart: listing.auctionStart,
        auctionEnd: listing.auctionEnd,
        status: listing.status,
        frontImage: listing.frontImage,
        backImage: listing.backImage
      })))
    );
  }
  
  getSoldListings(): Observable<Listing[]> {
    return this.http.get<any[]>('/api/marketplace/sold', { headers: this.getAuthHeaders() }).pipe(
      map(listings => listings.map(listing => ({
        id: listing.id,
        cardName: listing.cardName,
        cardSet: listing.cardSet,
        cardNumber: listing.cardNumber,
        overallGrade: listing.overallGrade,
        startingPrice: listing.startingPrice,
        buyoutPrice: listing.buyoutPrice,
        soldPrice: listing.soldPrice,
        soldDate: listing.soldDate,
        listingType: listing.listingType,
        auctionStart: listing.auctionStart,
        auctionEnd: listing.auctionEnd,
        status: listing.status,
        frontImage: listing.frontImage,
        backImage: listing.backImage
      })))
    );
  }

  placeBid(listingId: number, bidAmount: number): Observable<{ message: string, bidId: number, bidAmount: number }> {
    return this.http.post<{ message: string, bidId: number, bidAmount: number }>('/api/marketplace/bid', { listingId, bidAmount });}

  createAuctionBuyoutIntent(listingId: number): Observable<{ clientSecret: string }> {
    return this.http.post<{ clientSecret: string }>('/api/marketplace/auction/buyout-intent', { listingId });}

  completeAuctionBuyout(listingId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/marketplace/auction/buyout', { listingId });}

  createPaymentIntent(listingId: number, amount: number): Observable<{ clientSecret: string }> {
    return this.http.post<{ clientSecret: string }>('/api/marketplace/payment-intent', { listingId, amount });}

  completePurchase(listingId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/marketplace/purchase', { listingId });}

  checkGoogleAuth(): Observable<{ isAuthorized: boolean }> {
    return this.http.get<{ isAuthorized: boolean }>('/api/marketplace/check-auth', { withCredentials: true });}

  exportToGoogleSheets(): Observable<{ message: string, spreadsheetUrl?: string }> {
    return this.http.post<{ message: string, spreadsheetUrl?: string }>('/api/marketplace/export-sold', {}, { withCredentials: true });}

  exportSoldListings(): Observable<any> {
    return this.http.get('/api/marketplace/export-sold', { headers: this.getAuthHeaders() });}
}

export interface Listing {
  id: number;
  userId?: number;
  cardName: string;
  cardSet: string;
  cardNumber: string;
  overallGrade: number;
  startingPrice?: number;
  buyoutPrice?: number;
  soldPrice?: number;
  soldDate?: string; // ISO string, e.g., "2025-03-23T12:34:56"
  listingType: string;
  auctionStart?: string;
  auctionEnd?: string;
  status?: string;
  frontImage?: string; // Base64 string
  backImage?: string; // Base64 string
}