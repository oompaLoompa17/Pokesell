// favorites.store.ts
import { Injectable } from '@angular/core';
import { ComponentStore } from '@ngrx/component-store';
import { PokemonService } from '../../services/pokemon.service';
import { switchMap, tap, catchError } from 'rxjs/operators';
import { Observable, of } from 'rxjs';

interface FavoritesState {
  favorites: any[];
  message: string | null;
  error: string | null;
  loading: boolean;
}

@Injectable()
export class FavoritesStore extends ComponentStore<FavoritesState> {
  constructor(private pokemonService: PokemonService) {
    super({
      favorites: [],
      message: null,
      error: null,
      loading: false,
    });
  }

  // Selectors
  readonly favorites$ = this.select(state => state.favorites);
  readonly message$ = this.select(state => state.message);
  readonly error$ = this.select(state => state.error);
  readonly loading$ = this.select(state => state.loading);

  // Updaters
  readonly setLoading = this.updater((state, loading: boolean) => ({
    ...state,
    loading,
  }));

  readonly setFavorites = this.updater((state, favorites: any[]) => ({
    ...state,
    favorites,
    loading: false,
    error: null,
  }));

  readonly setError = this.updater((state, error: string) => ({
    ...state,
    error,
    loading: false,
  }));

  readonly setMessage = this.updater((state, message: string) => ({
    ...state,
    message,
    error: null,
  }));

  // Effects
  readonly loadFavorites = this.effect((trigger$: Observable<void>) =>
    trigger$.pipe(
      tap(() => this.setLoading(true)),
      switchMap(() =>
        this.pokemonService.getFavorites().pipe(
          tap(response => this.setFavorites(response.data)),
          catchError(err => {
            this.setError(err.error?.message || 'Failed to load favorites');
            return of(null);
          })
        )
      )
    )
  );

  readonly removeFromFavorites = this.effect((cardId$: Observable<string>) =>
    cardId$.pipe(
      switchMap(cardId =>
        this.pokemonService.removeFromFavorites(cardId).pipe(
          tap(response => {
            this.setMessage(response.message);
            this.setFavorites(this.get().favorites.filter(card => card.id !== cardId));
          }),
          catchError(err => {
            this.setError(err.error?.message || 'Failed to remove from favorites');
            return of(null);
          })
        )
      )
    )
  );
}