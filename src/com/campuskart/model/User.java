package com.campuskart.model;

// ★ Asg 12: user-defined package com.campuskart.model
//
// ★ RUBRIC: Inheritance (1M)
//   Asg 10 : abstract class + abstract method
//   Asg 9  : final variable (userId) and final method (getUserId)
//   Asg 2  : default and parameterized constructors
//   Asg 8  : subclasses chain to this constructor with super(...)
public abstract class User {

    private final int userId;                     // ★ Asg 9: final variable — set once, never reassigned
    protected String fullName, email, password, role;

    public User() {                               // ★ Asg 2: default constructor
        this.userId = 0;
    }

    public User(int userId, String fullName, String email, String role) {   // ★ Asg 2: parameterized constructor
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public final int getUserId() { return userId; }   // ★ Asg 9: final method — subclasses cannot override identity

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    // ★ Asg 10: abstract method — every subclass MUST provide its own dashboard path
    public abstract String getDashboardPath();
}
