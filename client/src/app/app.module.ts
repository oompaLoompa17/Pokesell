import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { HTTP_INTERCEPTORS, HttpClientModule, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { LoginComponent } from './components/login/login.component';
import { SearchComponent } from './components/search/search.component';
import { SearchResultsComponent } from './components/search-results/search-results.component';
import { FavoritesComponent } from './components/favorites/favorites.component';
import { CardDetailsComponent } from './components/card-details/card-details.component';
import { RegisterComponent } from './components/register/register.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { RouterModule, Routes } from '@angular/router';
import { AuthInterceptor } from './auth.interceptor';
import { NavbarComponent } from './components/navbar/navbar.component';
import { CreateListingComponent } from './components/create-listing/create-listing.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'; // Required for Material animations
import { MatToolbarModule } from '@angular/material/toolbar'; // For mat-toolbar
import { MatButtonModule } from '@angular/material/button'; // For mat-button, mat-raised-button
import { MatFormFieldModule } from '@angular/material/form-field'; // For mat-form-field
import { MatInputModule } from '@angular/material/input'; // For matInput
import { MatSelectModule } from '@angular/material/select'; // For mat-select
import { MatCardModule } from '@angular/material/card'; // For mat-card
import { MatListModule } from '@angular/material/list';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { FixedListingsComponent } from './components/fixed-listings/fixed-listings.component';
import { AuctionListingsComponent } from './components/auction-listings/auction-listings.component';
import { PurchaseComponent } from './components/purchase/purchase.component';
import { AuctionPurchaseComponent } from './components/auction-purchase/auction-purchase.component';

const routes: Routes = [
  { path: '', component: LoginComponent }, 
  { path: 'search', component: SearchComponent },
  { path: 'cards/search', component: SearchResultsComponent },
  { path: 'favorites', component: FavoritesComponent },
  { path: 'cards/:id', component: CardDetailsComponent },
  { path: 'marketplace/fixed', component: FixedListingsComponent }, 
  { path: 'marketplace/auctions', component: AuctionListingsComponent }, 
  { path: 'marketplace/auction-purchase/:listingId', component: AuctionPurchaseComponent }, 
  { path: 'marketplace/purchase/:listingId', component: PurchaseComponent },
  { path: 'create-listing', component: CreateListingComponent },
  { path: 'register', component: RegisterComponent },
  { path: '**', component: NotFoundComponent } // 404 route
];

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    SearchComponent,
    SearchResultsComponent,
    FavoritesComponent,
    CardDetailsComponent,
    RegisterComponent,
    NotFoundComponent,
    FavoritesComponent,
    CardDetailsComponent,
    RegisterComponent,
    NotFoundComponent,
    NavbarComponent,
    CreateListingComponent,
    FixedListingsComponent,
    AuctionListingsComponent,
    PurchaseComponent,
    AuctionPurchaseComponent,
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    RouterModule.forRoot(routes),
    HttpClientModule,
    BrowserAnimationsModule, 
    MatToolbarModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatListModule,
    MatTableModule,
    MatTabsModule,
    MatGridListModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  exports: [
    RouterModule
  ],
  // provideHttpClient(withInterceptorsFromDi()), 
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
