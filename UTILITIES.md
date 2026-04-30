# Utilities Documentation

## Overview

Utility classes provide common functionality used across the application, particularly database connectivity and potentially shared helper methods.

**Location**: `src/util/`

## Utility Classes

### 1. DBConnection

Core utility class for database connectivity management.

```java
public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/pwqpvs_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Roshan25";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

**Responsibility**: Provide centralized database connection management

---

#### Connection String

```
jdbc:postgresql://localhost:5432/pwqpvs_db
```

**Components**:
| Component | Value | Description |
|-----------|-------|-------------|
| Protocol | jdbc:postgresql:// | PostgreSQL JDBC driver |
| Host | localhost | Database server (local machine) |
| Port | 5432 | PostgreSQL default port |
| Database | pwqpvs_db | Database name |

---

#### Configuration

**Database Credentials**:
```java
String URL = "jdbc:postgresql://localhost:5432/pwqpvs_db";
String USER = "postgres";
String PASSWORD = "Roshan25";
```

**Current Issues**: ⚠️
- Credentials hardcoded in source code
- Password visible in version control
- Same credentials for all environments

**Required Changes for Production**:
```java
// Environment variable approach
String url = System.getenv("DB_URL");
String user = System.getenv("DB_USER");
String password = System.getenv("DB_PASSWORD");

// Or properties file approach
Properties props = new Properties();
props.load(new FileInputStream("application.properties"));
String url = props.getProperty("db.url");
String user = props.getProperty("db.user");
String password = props.getProperty("db.password");
```

---

#### Usage Pattern

**Try-with-Resources (Modern Java)**:
```java
// Connection automatically closed
try (Connection conn = DBConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    
    ps.setString(1, value);
    ResultSet rs = ps.executeQuery();
    
    while (rs.next()) {
        // Process results
    }
    
} catch (SQLException e) {
    System.out.println("Error: " + e.getMessage());
}
```

**Manual Resource Management (Older Java)**:
```java
Connection conn = null;
PreparedStatement ps = null;
ResultSet rs = null;

try {
    conn = DBConnection.getConnection();
    ps = conn.prepareStatement(sql);
    rs = ps.executeQuery();
    
} catch (SQLException e) {
    System.out.println("Error: " + e.getMessage());
} finally {
    // Must close in reverse order
    try {
        if (rs != null) rs.close();
        if (ps != null) ps.close();
        if (conn != null) conn.close();
    } catch (SQLException e) {
        System.out.println("Cleanup error: " + e.getMessage());
    }
}
```

---

#### Connection Pooling Consideration

**Current Implementation**: Creates new connection per request

```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);  // New connection every time
}
```

**Performance Impact**:
- Creating connection takes time (overhead)
- High load → Many connections → Resource exhaustion
- Not suitable for high-traffic systems

**Recommended Improvement**: Connection pooling

```java
// Using HikariCP (popular, high-performance)
private static HikariDataSource dataSource;

static {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(URL);
    config.setUsername(USER);
    config.setPassword(PASSWORD);
    config.setMaximumPoolSize(10);      // Max 10 connections
    config.setMinimumIdle(2);           // Min 2 idle connections
    config.setConnectionTimeout(20000); // 20 second timeout
    dataSource = new HikariDataSource(config);
}

public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();  // Reuse from pool
}
```

**Connection Pool Benefits**:
- Reuses connections (faster)
- Limits connection count
- Better resource management
- Improved throughput

---

#### Database Driver

The application uses PostgreSQL JDBC driver:
```
org.postgresql:postgresql:42.x.x
```

Must be in classpath for application to run.

**Driver Registration** (automatic in modern versions):
```java
// Not needed in JDBC 4.0+ - auto-loaded
// Class.forName("org.postgresql.Driver");
```

---

#### Common Connection Errors

| Error | Cause | Solution |
|-------|-------|----------|
| Connection refused | PostgreSQL not running | Start PostgreSQL service |
| Invalid password | Wrong credentials | Update DBConnection.java |
| Database does not exist | Database not created | Run schema.sql |
| Unknown host | Wrong hostname | Verify localhost/IP |
| Port in use | Wrong port number | Verify port 5432 |
| Driver not found | Missing JDBC driver | Add JAR to classpath |

---

#### Testing Database Connection

**Quick Test Code**:
```java
try {
    Connection conn = DBConnection.getConnection();
    if (conn != null) {
        System.out.println("✓ Database connected successfully");
        
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next()) {
            System.out.println("✓ User count: " + rs.getInt(1));
        }
        conn.close();
    }
} catch (SQLException e) {
    System.out.println("✗ Connection failed: " + e.getMessage());
    e.printStackTrace();
}
```

---

## Potential Future Utilities

### 1. Logger Utility

Centralized logging configuration:
```java
public class Logger {
    private static final java.util.logging.Logger LOGGER = 
        java.util.logging.Logger.getLogger(Logger.class.getName());
    
    public static void info(String message) {
        LOGGER.info(message);
    }
    
    public static void error(String message, Exception e) {
        LOGGER.log(Level.SEVERE, message, e);
    }
}
```

**Usage**:
```java
Logger.info("Payment released for milestone #5");
Logger.error("Database connection failed", e);
```

---

### 2. Validation Utility

Input validation helpers:
```java
public class Validator {
    public static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    public static boolean isPositiveNumber(double num) {
        return num > 0;
    }
    
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }
}
```

**Usage**:
```java
if (!Validator.isValidEmail(email)) {
    JOptionPane.showMessageDialog(this, "Invalid email format");
    return;
}
```

---

### 3. Currency Formatter Utility

Format currency consistently:
```java
public class CurrencyFormatter {
    private static final NumberFormat CURRENCY_FORMAT = 
        NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    
    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount);  // ₹10,000.00
    }
}
```

**Usage**:
```java
label.setText(CurrencyFormatter.format(50000.00));  // ₹50,000.00
```

---

### 4. Date/Time Utility

Consistent date formatting:
```java
public class DateTimeFormatter {
    private static final java.text.SimpleDateFormat SDF = 
        new java.text.SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    
    public static String format(java.util.Date date) {
        return SDF.format(date);
    }
}
```

**Usage**:
```java
String timestamp = DateTimeFormatter.format(new java.util.Date());
// 15-Apr-2026 14:30:45
```

---

### 5. Exception Handling Utility

Centralized exception handling:
```java
public class ExceptionHandler {
    public static String getErrorMessage(SQLException e) {
        if (e.getMessage().contains("connection refused")) {
            return "Database connection failed. Is PostgreSQL running?";
        } else if (e.getMessage().contains("password")) {
            return "Database password incorrect. Check DBConnection.java";
        }
        return "Database error: " + e.getMessage();
    }
}
```

**Usage**:
```java
catch (SQLException e) {
    String userMessage = ExceptionHandler.getErrorMessage(e);
    JOptionPane.showMessageDialog(this, userMessage);
}
```

---

### 6. Configuration Utility

Load configuration from file:
```java
public class Config {
    private static Properties props = new Properties();
    
    static {
        try {
            props.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Config file not found");
        }
    }
    
    public static String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
```

**config.properties**:
```
db.url=jdbc:postgresql://localhost:5432/pwqpvs_db
db.user=postgres
app.title=Nirikshan - Construction Payment System
app.version=1.0.0
```

---

## Best Practices

### ✅ DO

1. **Use DBConnection for all database access**
   ```java
   Connection conn = DBConnection.getConnection();  // Central point
   ```

2. **Close resources properly**
   ```java
   try (Connection conn = DBConnection.getConnection()) {
       // Automatically closed
   }
   ```

3. **Handle exceptions explicitly**
   ```java
   catch (SQLException e) {
       System.out.println("Error: " + e.getMessage());
   }
   ```

4. **Use try-with-resources in Java 7+**
   ```java
   try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
       // Code
   }
   ```

### ❌ DON'T

1. **Don't hardcode connection strings**
   ```java
   // ❌ Wrong
   Connection conn = DriverManager.getConnection(
       "jdbc:postgresql://localhost:5432/pwqpvs_db", 
       "postgres", 
       "Roshan25"
   );
   ```

2. **Don't forget to close connections**
   ```java
   // ❌ Wrong - resource leak
   Connection conn = DBConnection.getConnection();
   // Use conn but never close...
   ```

3. **Don't expose credentials**
   ```java
   // ❌ Wrong - in logs/version control
   System.out.println("Connecting as: " + user + "/" + password);
   ```

4. **Don't create utilities without clear purpose**
   ```java
   // ❌ Unnecessary utility
   public class MathUtils {
       public static int add(int a, int b) {
           return a + b;  // Just use native operators
       }
   }
   ```

---

## Utility Design Guidelines

### Single Responsibility Principle

Each utility should have one clear purpose:
- ✅ DBConnection: Database connectivity
- ✅ Logger: Logging events
- ❌ Utils: Everything mixed together

### Stateless Design

Utilities should not maintain state:
```java
// ✅ Good - stateless
public class CurrencyFormatter {
    public static String format(double amount) {
        return amount;
    }
}

// ❌ Bad - maintains state
public class CurrencyFormatter {
    private double lastAmount;  // State
}
```

### Testability

Utilities should be easily testable:
```java
// ✅ Testable
public class Validator {
    public static boolean isValidEmail(String email) {
        return email.matches(pattern);
    }
}

// Test
assertTrue(Validator.isValidEmail("test@example.com"));
assertFalse(Validator.isValidEmail("invalid"));
```

---

## Migration Path for Utilities

**Phase 1** (Current):
- DBConnection only
- Hardcoded credentials

**Phase 2** (Near-term):
- Add Logger utility
- Add Validator utility
- Move credentials to config file

**Phase 3** (Medium-term):
- Add CurrencyFormatter
- Add DateTimeFormatter
- Add ExceptionHandler

**Phase 4** (Long-term):
- Connection pooling in DBConnection
- Caching layer
- Performance monitoring utilities
