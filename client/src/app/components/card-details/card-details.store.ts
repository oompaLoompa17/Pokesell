// card-details.store.ts
import { Injectable } from '@angular/core';
import { ComponentStore } from '@ngrx/component-store';
import { PokemonService } from '../../services/pokemon.service';
import { switchMap, tap, catchError } from 'rxjs/operators';
import { Observable, of } from 'rxjs';

interface CardDetailsState {
  card: any | null;
  message: string | null;
  error: string | null;
  loading: boolean;
}

@Injectable()
export class CardDetailsStore extends ComponentStore<CardDetailsState> {
  constructor(private pokemonService: PokemonService) {
    super({
      card: null,
      message: null,
      error: null,
      loading: false,
    });
  }

  // Selectors
  readonly card$ = this.select(state => state.card);
  readonly message$ = this.select(state => state.message);
  readonly error$ = this.select(state => state.error);
  readonly loading$ = this.select(state => state.loading);

  // Updaters
  readonly setLoading = this.updater((state, loading: boolean) => ({
    ...state,
    loading,
  }));

  readonly setCard = this.updater((state, card: any) => ({
    ...state,
    card,
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
  readonly loadCardDetails = this.effect((cardId$: Observable<string>) =>
    cardId$.pipe(
      tap(() => this.setLoading(true)),
      switchMap(cardId =>
        this.pokemonService.getCardDetails(cardId).pipe(
          tap(card => this.setCard(card)),
          catchError(err => {
            this.setError(err.error || 'Failed to load card details');
            return of(null);
          })
        )
      )
    )
  );

  readonly addToFavorites = this.effect((cardId$: Observable<string>) =>
    cardId$.pipe(
      switchMap(cardId =>
        this.pokemonService.addToFavorites(cardId).pipe(
          tap(() => this.setMessage('Added to favorites!')),
          catchError(err => {
            this.setError(err.error || 'Failed to add to favorites');
            return of(null);
          })
        )
      )
    )
  );
}