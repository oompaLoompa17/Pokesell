import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: false,
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.css']
})
export class NotFoundComponent {
  searchForm: FormGroup;
  errorMessage = 'The page you’re looking for doesn’t exist or may have been removed.';

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {
    this.searchForm = this.fb.group({
      name: ['', Validators.required]
    });
  }

  onSearch() {
    if (this.searchForm.valid) {
      this.router.navigate(['/cards/search'], { queryParams: this.searchForm.value });
    }
  }
}