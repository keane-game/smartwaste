import { AbstractControl } from '@angular/forms';

/**
 * validation function
 * birth date should be less than 10 years
 * @param control
 */
export function birthDateValidator(control: AbstractControl) {
    const value = control.value;
    const today = new Date();
    const birthDate = new Date(value);
    let age = today.getFullYear() - birthDate.getFullYear();
    const min = 10;
    if (age > min + 1) {
        return null;
    }
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }
    const reuslt = age >= min ? null : { 'invalidBirthDate': true };
    return reuslt;
}
