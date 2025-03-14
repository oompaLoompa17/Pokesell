import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  error: string | null = null;

  passwordCriteria = {
    minLength: false,
    uppercase: false,
    number: false,
    specialChar: false // New criterion
  };

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.registerForm.get('password')?.valueChanges.subscribe(value => {
      this.updatePasswordCriteria(value);
    });
  }

  updatePasswordCriteria(password: string) {
    this.passwordCriteria.minLength = password?.length >= 6;
    this.passwordCriteria.uppercase = /[A-Z]/.test(password);
    this.passwordCriteria.number = /\d/.test(password);
    this.passwordCriteria.specialChar = /[!@#$%^&*()_+=|<>?{}\[\]~-]/.test(password); // Match backend regex
  }

  isFormValid(): boolean {
    const emailValid = this.registerForm.get('email')?.valid;
    const passwordValid = this.registerForm.get('password')?.valid &&
                         this.passwordCriteria.uppercase &&
                         this.passwordCriteria.number &&
                         this.passwordCriteria.specialChar;
    return !!(emailValid && passwordValid);
  }

  onSubmit() {
    if (this.isFormValid()) {
      this.authService.register(this.registerForm.value).subscribe({
        next: () => this.router.navigate([''], { 
          queryParams: { success: 'You have registered successfully, please proceed to login' }
        }),
        error: (err) => {
          // Handle validation errors from Spring
          const errorMsg = err.error?.errors?.[0]?.message || err.error?.message || 'Registration failed';
          this.error = errorMsg;
        }
      });
    }
  }
}