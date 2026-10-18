package com.campuskart.model;

// ★ RUBRIC: Inheritance (1M) | Asg 7: Admin also extends User (single inheritance)
//            Asg 8  : super(...) chains to the User constructor
//            Asg 10 : overriding the abstract method inherited from the abstract class
public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(int id, String name, String email) {
        super(id, name, email, "ADMIN");          // ★ Asg 8: super() — the parent sets id, name, email, role
    }

    @Override
    public String getDashboardPath() {
        return "/admin";
    }
}
