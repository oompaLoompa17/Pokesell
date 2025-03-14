// card-details.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CardDetailsStore } from './card-details.store';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-card-details',
  standalone: false,
  templateUrl: './card-details.component.html',
  styleUrls: ['./card-details.component.css'],
  providers: [CardDetailsStore], // Provide store at component level
})
export class CardDetailsComponent implements OnInit {
  card$: Observable<any | null>;
  message$: Observable<string | null>;
  error$: Observable<string | null>;
  loading$: Observable<boolean>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private store: CardDetailsStore
  ) {
    this.card$ = this.store.card$;
    this.message$ = this.store.message$;
    this.error$ = this.store.error$;
    this.loading$ = this.store.loading$;
  }

  ngOnInit() {
    const cardId = this.route.snapshot.paramMap.get('id');
    if (cardId) {
      this.store.loadCardDetails(cardId);
    }
  }

  addToFavorites(cardId: string) {
    this.store.addToFavorites(cardId);
  }

  goBack() {
    this.router.navigate(['/search']);
  }
}
// import { Component, OnInit } from '@angular/core';
// import { ActivatedRoute, Router } from '@angular/router';
// import { PokemonService } from '../../services/pokemon.service';

// @Component({
//   selector: 'app-card-details',
//   standalone: false,
//   templateUrl: './card-details.component.html',
//   styleUrls: ['./card-details.component.css'],
// })
// export class CardDetailsComponent implements OnInit {
//   card: any = null;
//   message: string | null = null;
//   error: string | null = null;

//   constructor(
//     private route: ActivatedRoute,
//     private router: Router,
//     private pokemonService: PokemonService
//   ) {}

//   ngOnInit() {
//     const cardId = this.route.snapshot.paramMap.get('id');
//     if (cardId) {
//       this.pokemonService.getCardDetails(cardId).subscribe({
//         next: (card) => this.card = card,
//         error: (err) => this.error = err.error || 'Failed to load card details'
//       });
//     }
//   }

//   addToFavorites(cardId: string) {
//     this.pokemonService.addToFavorites(cardId).subscribe({
//       next: () => this.message = 'Added to favorites!',
//       error: (err) => this.error = err.error || 'Failed to add to favorites'
//     });
//   }

//   goBack() {
//     this.router.navigate(['/search']);
//   }
// }

