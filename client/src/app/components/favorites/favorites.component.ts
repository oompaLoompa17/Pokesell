// favorites.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FavoritesStore } from './favorites.store';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-favorites',
  standalone: false,
  templateUrl: './favorites.component.html',
  styleUrls: ['./favorites.component.css'],
  providers: [FavoritesStore],
})
export class FavoritesComponent implements OnInit {
  favorites$: Observable<any[]>;
  message$: Observable<string | null>;
  error$: Observable<string | null>;
  loading$: Observable<boolean>;
  
  constructor(private store: FavoritesStore, private router: Router) {
    this.favorites$ = this.store.favorites$;
    this.message$ = this.store.message$;
    this.error$ = this.store.error$;
    this.loading$ = this.store.loading$;
  }

  ngOnInit() {
    this.store.loadFavorites();
  }

  removeFromFavorites(cardId: string) {
    this.store.removeFromFavorites(cardId);
  }

  goBack() {
    this.router.navigate(['/search']);
  }
}

// import { Component, OnInit } from '@angular/core';
// import { Router } from '@angular/router';
// import { PokemonService } from '../../services/pokemon.service';
// import { Observable } from 'rxjs';

// @Component({
//   selector: 'app-favorites',
//   standalone: false,
//   templateUrl: './favorites.component.html',
//   styleUrls: ['./favorites.component.css']
// })
// export class FavoritesComponent implements OnInit {
//   favorites: any[] = [];
//   message: string | null = null;
//   error: string | null = null;

//   constructor(private pokemonService: PokemonService, private router: Router) {}

//   ngOnInit() {
//     this.loadFavorites();
//   }

//   loadFavorites() {
//     this.pokemonService.getFavorites().subscribe({
//       next: (response) => this.favorites = response.data, // Access 'data' key
//       error: (err) => this.error = err.error?.message || 'Failed to load favorites'
//     });
//   }

//   removeFromFavorites(cardId: string) {
//     this.pokemonService.removeFromFavorites(cardId).subscribe({
//       next: (response) => {
//         this.message = response.message;
//         this.favorites = this.favorites.filter(card => card.id !== cardId);
//         this.pokemonService.getFavorites().subscribe({
//           next: (resp) => this.favorites = resp.data,
//           error: (err) => console.error('Error refreshing favorites:', err)
//         });
//       },
//       error: (err) => this.error = err.error?.message || 'Failed to remove from favorites'
//     });
//   }

//   goBack() {
//     this.router.navigate(['/search']);
//   }
// }