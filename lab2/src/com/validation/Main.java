package com.validation;

import com.validation.exception.ValidationException;
import com.validation.validator.Validator;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("--- Test 1 ---");
            Student student = new Student();
            student.setEmail("Grzegorz.Brzęczyszczykiewicz#pbs.edu.pl");
            Validator.validate(student);
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n--- Test 2 ---");
        try {
            Student student2 = new Student("A", "B", "123", "zlymail");
            Validator.validate(student2);
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n--- Test 3 ---");
        try {
            Student student3 = new Student("Jan", "Kowalski", "12345678", "jan.kowalski@pbs.edu.pl");
            Validator.validate(student3);
            System.out.println("Walidacja zakończona sukcesem (przykład prawidłowy).");
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("\n--- Test 4 ---");
        try {
            Student student4 = new Student("", "", "", "");
            Validator.validate(student4);
            System.out.println("Walidacja zakończona sukcesem (przykład prawidłowy).");
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }
    }
}
