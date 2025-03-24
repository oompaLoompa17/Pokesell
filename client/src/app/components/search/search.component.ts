import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PokemonService } from '../../services/pokemon.service';
import { FormGroup, FormBuilder } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-search',
  standalone: false,
  templateUrl: './search.component.html',
  styleUrl: './search.component.css'
})
export class SearchComponent implements OnInit{
  searchForm: FormGroup;
  sets: any[] = [];
  types: string[] = [];
  rarities: string[] = [];

  constructor(
    private fb: FormBuilder,
    private pokemonService: PokemonService,
    private router: Router,
    private authService: AuthService
  ) {
    this.searchForm = this.fb.group({
      name: [''],
      set: [''],
      type: [''],
      rarity: ['']
    });
  }

  ngOnInit() {
    this.pokemonService.getFilters().subscribe({
      next: (filters) => {
        this.sets = filters.sets;
        this.types = filters.types;
          this.rarities = filters.rarities;
      },
      error: (err) => {
        console.error('Error fetching filters:', err);
      }
    });
  }

  onSearch() {
    if (this.searchForm.valid) {
      this.router.navigate(['/cards/search'], { queryParams: this.searchForm.value });
    }
  }
}
