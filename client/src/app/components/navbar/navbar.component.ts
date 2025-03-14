import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  constructor(private authService: AuthService, private router: Router) {}

  isLoggedIn() {
    return !!localStorage.getItem('authToken');
  }

  logout() {
    this.authService.logout().subscribe({
      next: (response) => {
        console.log(response.message);
        this.router.navigate(['/']);
      },
      error: (err) => console.error('Logout failed:', err)
    });
  }
}