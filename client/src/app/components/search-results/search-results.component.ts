import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PokemonService } from '../../services/pokemon.service';

@Component({
  selector: 'app-search-results',
  standalone: false,
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css'
})
export class SearchResultsComponent {
  cards: any[] = [];
  message: string | null = null;
  error: string | null = null;
  currentPage = 1;
  totalPages = 1;
  pageSize = 10;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pokemonService: PokemonService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.currentPage = +params['page'] || 1;
      this.searchCards(params);
    });
  }

  searchCards(params: any) {
    this.pokemonService.searchCards({ ...params, page: this.currentPage, pageSize: this.pageSize }).subscribe({
      next: (response) => {
        this.cards = response.cards;
        this.totalPages = response.totalPages;
      },
      error: (err) => this.error = err.error || 'Search failed'
    });
  }

  changePage(page: number) {
    this.currentPage = page;
    this.router.navigate([], { queryParams: { page: this.currentPage }, queryParamsHandling: 'merge' });
  }

  addToFavorites(cardId: string) {
    console.log('Adding card:', cardId, 'Token:', localStorage.getItem('authToken'));
    this.pokemonService.addToFavorites(cardId).subscribe({
      next: () => this.message = 'Added to favorites!',
      error: (err) => this.error = err.error || 'Failed to add to favorites'
    });
  }

  goBack() {
    this.router.navigate(['/search']);
  }
}
