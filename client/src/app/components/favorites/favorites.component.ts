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