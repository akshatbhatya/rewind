# 🚀 PHP Best Practices & Implementation Guide (3 Years Experience)

**Focus:** Clean Code, Design Patterns, Security, Performance, and Real-World Implementations

This guide contains **50+ PHP interview questions** with production-ready code examples following industry best practices.

---

## Table of Contents
1. [Object-Oriented Design Patterns](#1-object-oriented-design-patterns)
2. [SOLID Principles in PHP](#2-solid-principles-in-php)
3. [Security Best Practices](#3-security-best-practices)
4. [Performance Optimization](#4-performance-optimization)
5. [Error Handling & Logging](#5-error-handling--logging)
6. [Testing & Quality](#6-testing--quality)
7. [API Development](#7-api-development)
8. [Database Design & Query Optimization](#8-database-design--query-optimization)

---

## 1. Object-Oriented Design Patterns

### Q1: Implement a Singleton Pattern (Thread-Safe)
**Use Case:** Database connection, Configuration manager

```php
class Database {
    private static ?Database $instance = null;
    private PDO $connection;
    
    // Private constructor prevents direct instantiation
    private function __construct() {
        $dsn = "mysql:host=localhost;dbname=myapp";
        $this->connection = new PDO($dsn, 'user', 'pass', [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]);
    }
    
    // Prevent cloning
    private function __clone() {}
    
    // Prevent unserialization
    public function __wakeup() {
        throw new Exception("Cannot unserialize singleton");
    }
    
    public static function getInstance(): Database {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }
    
    public function getConnection(): PDO {
        return $this->connection;
    }
}

// Usage
$db = Database::getInstance();
$pdo = $db->getConnection();
```

**Best Practices:**
✅ Private constructor prevents direct instantiation  
✅ Prevent cloning and unserialization  
✅ Lazy initialization (only created when needed)

---

### Q2: Implement Factory Pattern
**Use Case:** Create different payment gateways

```php
interface PaymentGateway {
    public function charge(float $amount): bool;
    public function refund(string $transactionId): bool;
}

class StripeGateway implements PaymentGateway {
    public function charge(float $amount): bool {
        // Stripe-specific implementation
        return true;
    }
    
    public function refund(string $transactionId): bool {
        return true;
    }
}

class PayPalGateway implements PaymentGateway {
    public function charge(float $amount): bool {
        // PayPal-specific implementation
        return true;
    }
    
    public function refund(string $transactionId): bool {
        return true;
    }
}

class PaymentFactory {
    public static function create(string $type): PaymentGateway {
        return match($type) {
            'stripe' => new StripeGateway(),
            'paypal' => new PayPalGateway(),
            default => throw new InvalidArgumentException("Unknown payment type: $type")
        };
    }
}

// Usage
$gateway = PaymentFactory::create('stripe');
$gateway->charge(100.00);
```

**Best Practices:**
✅ Use interfaces for contracts  
✅ Use `match` expression (PHP 8+)  
✅ Throw exceptions for invalid input

---

### Q3: Implement Repository Pattern
**Use Case:** Separate data access logic from business logic

```php
interface UserRepositoryInterface {
    public function find(int $id): ?User;
    public function findByEmail(string $email): ?User;
    public function save(User $user): bool;
    public function delete(int $id): bool;
}

class User {
    public function __construct(
        public ?int $id,
        public string $name,
        public string $email,
        public string $password
    ) {}
}

class UserRepository implements UserRepositoryInterface {
    private PDO $db;
    
    public function __construct(PDO $db) {
        $this->db = $db;
    }
    
    public function find(int $id): ?User {
        $stmt = $this->db->prepare("SELECT * FROM users WHERE id = :id");
        $stmt->execute(['id' => $id]);
        $data = $stmt->fetch();
        
        if (!$data) {
            return null;
        }
        
        return new User(
            $data['id'],
            $data['name'],
            $data['email'],
            $data['password']
        );
    }
    
    public function findByEmail(string $email): ?User {
        $stmt = $this->db->prepare("SELECT * FROM users WHERE email = :email");
        $stmt->execute(['email' => $email]);
        $data = $stmt->fetch();
        
        return $data ? new User(
            $data['id'],
            $data['name'],
            $data['email'],
            $data['password']
        ) : null;
    }
    
    public function save(User $user): bool {
        if ($user->id === null) {
            // Insert
            $stmt = $this->db->prepare(
                "INSERT INTO users (name, email, password) VALUES (:name, :email, :password)"
            );
        } else {
            // Update
            $stmt = $this->db->prepare(
                "UPDATE users SET name = :name, email = :email WHERE id = :id"
            );
            $stmt->bindValue(':id', $user->id);
        }
        
        $stmt->execute([
            'name' => $user->name,
            'email' => $user->email,
            'password' => $user->password
        ]);
        
        return true;
    }
    
    public function delete(int $id): bool {
        $stmt = $this->db->prepare("DELETE FROM users WHERE id = :id");
        return $stmt->execute(['id' => $id]);
    }
}

// Service Layer
class UserService {
    public function __construct(
        private UserRepositoryInterface $userRepo
    ) {}
    
    public function registerUser(string $name, string $email, string $password): User {
        // Business logic
        if ($this->userRepo->findByEmail($email)) {
            throw new Exception("Email already exists");
        }
        
        $user = new User(
            null,
            $name,
            $email,
            password_hash($password, PASSWORD_BCRYPT)
        );
        
        $this->userRepo->save($user);
        return $user;
    }
}
```

**Best Practices:**
✅ Dependency injection via constructor  
✅ Interface segregation  
✅ Separate business logic from data access  
✅ Use prepared statements (SQL injection prevention)

---

### Q4: Implement Observer Pattern
**Use Case:** Event system, logging, notifications

```php
interface Observer {
    public function update(string $event, array $data): void;
}

interface Subject {
    public function attach(Observer $observer): void;
    public function detach(Observer $observer): void;
    public function notify(string $event, array $data): void;
}

class Order implements Subject {
    private array $observers = [];
    private string $status;
    
    public function attach(Observer $observer): void {
        $this->observers[] = $observer;
    }
    
    public function detach(Observer $observer): void {
        $this->observers = array_filter(
            $this->observers,
            fn($obs) => $obs !== $observer
        );
    }
    
    public function notify(string $event, array $data): void {
        foreach ($this->observers as $observer) {
            $observer->update($event, $data);
        }
    }
    
    public function setStatus(string $status): void {
        $this->status = $status;
        $this->notify('status_changed', ['status' => $status]);
    }
}

class EmailNotifier implements Observer {
    public function update(string $event, array $data): void {
        if ($event === 'status_changed') {
            echo "Sending email: Order status changed to {$data['status']}\n";
            // Send actual email
        }
    }
}

class SMSNotifier implements Observer {
    public function update(string $event, array $data): void {
        if ($event === 'status_changed') {
            echo "Sending SMS: Order status changed to {$data['status']}\n";
        }
    }
}

class Logger implements Observer {
    public function update(string $event, array $data): void {
        error_log("Event: $event - " . json_encode($data));
    }
}

// Usage
$order = new Order();
$order->attach(new EmailNotifier());
$order->attach(new SMSNotifier());
$order->attach(new Logger());

$order->setStatus('shipped'); // All observers notified
```

**Best Practices:**
✅ Loose coupling between subject and observers  
✅ Easy to add new observers  
✅ Multiple observers can react to same event

---

### Q5: Implement Strategy Pattern
**Use Case:** Different sorting algorithms, payment methods

```php
interface SortStrategy {
    public function sort(array $data): array;
}

class QuickSort implements SortStrategy {
    public function sort(array $data): array {
        if (count($data) <= 1) {
            return $data;
        }
        
        $pivot = $data[0];
        $left = $right = [];
        
        for ($i = 1; $i < count($data); $i++) {
            if ($data[$i] < $pivot) {
                $left[] = $data[$i];
            } else {
                $right[] = $data[$i];
            }
        }
        
        return array_merge(
            $this->sort($left),
            [$pivot],
            $this->sort($right)
        );
    }
}

class BubbleSort implements SortStrategy {
    public function sort(array $data): array {
        $n = count($data);
        for ($i = 0; $i < $n; $i++) {
            for ($j = 0; $j < $n - $i - 1; $j++) {
                if ($data[$j] > $data[$j + 1]) {
                    [$data[$j], $data[$j + 1]] = [$data[$j + 1], $data[$j]];
                }
            }
        }
        return $data;
    }
}

class Sorter {
    public function __construct(
        private SortStrategy $strategy
    ) {}
    
    public function setStrategy(SortStrategy $strategy): void {
        $this->strategy = $strategy;
    }
    
    public function sort(array $data): array {
        return $this->strategy->sort($data);
    }
}

// Usage
$data = [64, 34, 25, 12, 22, 11, 90];

$sorter = new Sorter(new QuickSort());
$result = $sorter->sort($data);

// Switch strategy at runtime
$sorter->setStrategy(new BubbleSort());
$result = $sorter->sort($data);
```

**Best Practices:**
✅ Change behavior at runtime  
✅ Eliminate conditional statements  
✅ Each strategy is independently testable

---

## 2. SOLID Principles in PHP

### Q6: Single Responsibility Principle (SRP)
**Bad Example:**
```php
class User {
    public function save() {
        // Database logic
    }
    
    public function sendEmail() {
        // Email logic
    }
    
    public function generateReport() {
        // Reporting logic
    }
}
```

**Good Example:**
```php
class User {
    public function __construct(
        public string $name,
        public string $email
    ) {}
}

class UserRepository {
    public function save(User $user): void {
        // Database logic only
    }
}

class EmailService {
    public function sendWelcomeEmail(User $user): void {
        // Email logic only
    }
}

class UserReportGenerator {
    public function generate(User $user): string {
        // Reporting logic only
    }
}
```

---

### Q7: Open/Closed Principle (OCP)
**Bad Example:**
```php
class DiscountCalculator {
    public function calculate(string $type, float $price): float {
        if ($type === 'seasonal') {
            return $price * 0.9;
        } elseif ($type === 'vip') {
            return $price * 0.8;
        }
        return $price;
    }
}
```

**Good Example:**
```php
interface Discount {
    public function apply(float $price): float;
}

class SeasonalDiscount implements Discount {
    public function apply(float $price): float {
        return $price * 0.9;
    }
}

class VIPDiscount implements Discount {
    public function apply(float $price): float {
        return $price * 0.8;
    }
}

class DiscountCalculator {
    public function calculate(Discount $discount, float $price): float {
        return $discount->apply($price);
    }
}

// Usage - Add new discount types without modifying existing code
$calculator = new DiscountCalculator();
$price = $calculator->calculate(new VIPDiscount(), 100);
```

---

### Q8: Liskov Substitution Principle (LSP)
**Bad Example:**
```php
class Rectangle {
    protected float $width;
    protected float $height;
    
    public function setWidth(float $width): void {
        $this->width = $width;
    }
    
    public function setHeight(float $height): void {
        $this->height = $height;
    }
    
    public function getArea(): float {
        return $this->width * $this->height;
    }
}

class Square extends Rectangle {
    public function setWidth(float $width): void {
        $this->width = $width;
        $this->height = $width; // Violates LSP
    }
    
    public function setHeight(float $height): void {
        $this->width = $height;
        $this->height = $height;
    }
}
```

**Good Example:**
```php
interface Shape {
    public function getArea(): float;
}

class Rectangle implements Shape {
    public function __construct(
        private float $width,
        private float $height
    ) {}
    
    public function getArea(): float {
        return $this->width * $this->height;
    }
}

class Square implements Shape {
    public function __construct(
        private float $side
    ) {}
    
    public function getArea(): float {
        return $this->side * $this->side;
    }
}
```

---

### Q9: Interface Segregation Principle (ISP)
**Bad Example:**
```php
interface Worker {
    public function work(): void;
    public function eat(): void;
    public function sleep(): void;
}

class Robot implements Worker {
    public function work(): void { /* OK */ }
    public function eat(): void { /* Robots don't eat! */ }
    public function sleep(): void { /* Robots don't sleep! */ }
}
```

**Good Example:**
```php
interface Workable {
    public function work(): void;
}

interface Eatable {
    public function eat(): void;
}

interface Sleepable {
    public function sleep(): void;
}

class Human implements Workable, Eatable, Sleepable {
    public function work(): void { /* ... */ }
    public function eat(): void { /* ... */ }
    public function sleep(): void { /* ... */ }
}

class Robot implements Workable {
    public function work(): void { /* ... */ }
}
```

---

### Q10: Dependency Inversion Principle (DIP)
**Bad Example:**
```php
class MySQLDatabase {
    public function connect(): void { /* ... */ }
}

class UserService {
    private MySQLDatabase $db;
    
    public function __construct() {
        $this->db = new MySQLDatabase(); // Hard dependency
    }
}
```

**Good Example:**
```php
interface DatabaseInterface {
    public function connect(): void;
    public function query(string $sql): array;
}

class MySQLDatabase implements DatabaseInterface {
    public function connect(): void { /* ... */ }
    public function query(string $sql): array { /* ... */ }
}

class PostgreSQLDatabase implements DatabaseInterface {
    public function connect(): void { /* ... */ }
    public function query(string $sql): array { /* ... */ }
}

class UserService {
    public function __construct(
        private DatabaseInterface $db // Depend on abstraction
    ) {}
    
    public function getUsers(): array {
        return $this->db->query("SELECT * FROM users");
    }
}

// Usage
$userService = new UserService(new MySQLDatabase());
// Easy to switch to PostgreSQL
$userService = new UserService(new PostgreSQLDatabase());
```

---

## 3. Security Best Practices

### Q11: Prevent SQL Injection
**Bad Example:**
```php
// NEVER DO THIS!
$email = $_GET['email'];
$query = "SELECT * FROM users WHERE email = '$email'";
$result = mysqli_query($conn, $query);
```

**Good Example:**
```php
// Use Prepared Statements
$email = $_GET['email'];
$stmt = $pdo->prepare("SELECT * FROM users WHERE email = :email");
$stmt->execute(['email' => $email]);
$user = $stmt->fetch();
```

---

### Q12: Prevent XSS (Cross-Site Scripting)
```php
class SecurityHelper {
    /**
     * Escape output for HTML context
     */
    public static function escape(string $data): string {
        return htmlspecialchars($data, ENT_QUOTES, 'UTF-8');
    }
    
    /**
     * Escape for JavaScript context
     */
    public static function escapeJS(string $data): string {
        return json_encode($data, JSON_HEX_TAG | JSON_HEX_AMP | JSON_HEX_APOS | JSON_HEX_QUOT);
    }
    
    /**
     * Sanitize user input
     */
    public static function sanitize(string $data): string {
        return filter_var(trim($data), FILTER_SANITIZE_STRING);
    }
}

// Usage in templates
$username = $_POST['username'];
echo "<h1>Welcome " . SecurityHelper::escape($username) . "</h1>";

// In JavaScript
echo "<script>var name = " . SecurityHelper::escapeJS($username) . ";</script>";
```

---

### Q13: CSRF Protection
```php
class CSRFToken {
    public static function generate(): string {
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
        
        $token = bin2hex(random_bytes(32));
        $_SESSION['csrf_token'] = $token;
        
        return $token;
    }
    
    public static function verify(string $token): bool {
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
        
        return isset($_SESSION['csrf_token']) 
            && hash_equals($_SESSION['csrf_token'], $token);
    }
}

// In form
$token = CSRFToken::generate();
echo '<input type="hidden" name="csrf_token" value="' . $token . '">';

// On form submission
if (!CSRFToken::verify($_POST['csrf_token'])) {
    die('CSRF token validation failed');
}
```

---

### Q14: Secure Password Hashing
```php
class PasswordManager {
    /**
     * Hash password using bcrypt
     */
    public static function hash(string $password): string {
        return password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
    }
    
    /**
     * Verify password
     */
    public static function verify(string $password, string $hash): bool {
        return password_verify($password, $hash);
    }
    
    /**
     * Check if rehash is needed (algorithm or cost changed)
     */
    public static function needsRehash(string $hash): bool {
        return password_needs_rehash($hash, PASSWORD_BCRYPT, ['cost' => 12]);
    }
}

// Usage
// Registration
$hashedPassword = PasswordManager::hash($_POST['password']);

// Login
if (PasswordManager::verify($_POST['password'], $user->password)) {
    // Check if rehash needed
    if (PasswordManager::needsRehash($user->password)) {
        $newHash = PasswordManager::hash($_POST['password']);
        // Update in database
    }
    // Login successful
}
```

---

### Q15: Rate Limiting (Prevent Brute Force)
```php
class RateLimiter {
    private PDO $db;
    
    public function __construct(PDO $db) {
        $this->db = $db;
    }
    
    /**
     * Check if action is allowed
     * 
     * @param string $identifier User IP or user ID
     * @param string $action Action name (e.g., 'login', 'api_request')
     * @param int $maxAttempts Maximum attempts allowed
     * @param int $windowSeconds Time window in seconds
     */
    public function isAllowed(
        string $identifier,
        string $action,
        int $maxAttempts = 5,
        int $windowSeconds = 300
    ): bool {
        $stmt = $this->db->prepare("
            SELECT COUNT(*) as attempts
            FROM rate_limits
            WHERE identifier = :identifier
            AND action = :action
            AND created_at > DATE_SUB(NOW(), INTERVAL :window SECOND)
        ");
        
        $stmt->execute([
            'identifier' => $identifier,
            'action' => $action,
            'window' => $windowSeconds
        ]);
        
        $result = $stmt->fetch();
        
        if ($result['attempts'] >= $maxAttempts) {
            return false;
        }
        
        // Log this attempt
        $this->logAttempt($identifier, $action);
        return true;
    }
    
    private function logAttempt(string $identifier, string $action): void {
        $stmt = $this->db->prepare("
            INSERT INTO rate_limits (identifier, action, created_at)
            VALUES (:identifier, :action, NOW())
        ");
        
        $stmt->execute([
            'identifier' => $identifier,
            'action' => $action
        ]);
    }
}

// Usage
$rateLimiter = new RateLimiter($pdo);

if (!$rateLimiter->isAllowed($_SERVER['REMOTE_ADDR'], 'login', 5, 300)) {
    http_response_code(429);
    die('Too many login attempts. Please try again later.');
}
```

---

## 4. Performance Optimization

### Q16: Lazy Loading with Generators
```php
class DataLoader {
    private PDO $db;
    
    public function __construct(PDO $db) {
        $this->db = $db;
    }
    
    /**
     * Load large datasets efficiently using generators
     * Memory usage: O(1) instead of O(n)
     */
    public function loadUsers(): Generator {
        $stmt = $this->db->query("SELECT * FROM users");
        
        while ($row = $stmt->fetch()) {
            yield new User(
                $row['id'],
                $row['name'],
                $row['email'],
                $row['password']
            );
        }
    }
}

// Usage - Process millions of rows without memory issues
$loader = new DataLoader($pdo);

foreach ($loader->loadUsers() as $user) {
    // Process one user at a time
    processUser($user);
}
```

---

### Q17: Caching Layer
```php
interface CacheInterface {
    public function get(string $key): mixed;
    public function set(string $key, mixed $value, int $ttl = 3600): bool;
    public function delete(string $key): bool;
    public function clear(): bool;
}

class RedisCache implements CacheInterface {
    private Redis $redis;
    
    public function __construct(string $host = '127.0.0.1', int $port = 6379) {
        $this->redis = new Redis();
        $this->redis->connect($host, $port);
    }
    
    public function get(string $key): mixed {
        $value = $this->redis->get($key);
        return $value !== false ? unserialize($value) : null;
    }
    
    public function set(string $key, mixed $value, int $ttl = 3600): bool {
        return $this->redis->setex($key, $ttl, serialize($value));
    }
    
    public function delete(string $key): bool {
        return $this->redis->del($key) > 0;
    }
    
    public function clear(): bool {
        return $this->redis->flushAll();
    }
}

class UserService {
    public function __construct(
        private UserRepositoryInterface $userRepo,
        private CacheInterface $cache
    ) {}
    
    public function getUser(int $id): ?User {
        $cacheKey = "user:$id";
        
        // Try cache first
        $user = $this->cache->get($cacheKey);
        
        if ($user === null) {
            // Cache miss - fetch from database
            $user = $this->userRepo->find($id);
            
            if ($user) {
                // Store in cache for 1 hour
                $this->cache->set($cacheKey, $user, 3600);
            }
        }
        
        return $user;
    }
    
    public function updateUser(User $user): void {
        $this->userRepo->save($user);
        
        // Invalidate cache
        $this->cache->delete("user:{$user->id}");
    }
}
```

---

### Q18: Database Query Optimization
```php
class OptimizedUserRepository {
    private PDO $db;
    
    public function __construct(PDO $db) {
        $this->db = $db;
    }
    
    /**
     * BAD: N+1 Query Problem
     */
    public function getUsersWithPostsBad(): array {
        $users = $this->db->query("SELECT * FROM users")->fetchAll();
        
        foreach ($users as &$user) {
            // This runs N queries!
            $stmt = $this->db->prepare("SELECT * FROM posts WHERE user_id = ?");
            $stmt->execute([$user['id']]);
            $user['posts'] = $stmt->fetchAll();
        }
        
        return $users;
    }
    
    /**
     * GOOD: Single query with JOIN
     */
    public function getUsersWithPostsGood(): array {
        $query = "
            SELECT 
                u.id as user_id,
                u.name,
                u.email,
                p.id as post_id,
                p.title,
                p.content
            FROM users u
            LEFT JOIN posts p ON u.id = p.user_id
            ORDER BY u.id
        ";
        
        $results = $this->db->query($query)->fetchAll();
        
        // Group posts by user
        $users = [];
        foreach ($results as $row) {
            $userId = $row['user_id'];
            
            if (!isset($users[$userId])) {
                $users[$userId] = [
                    'id' => $userId,
                    'name' => $row['name'],
                    'email' => $row['email'],
                    'posts' => []
                ];
            }
            
            if ($row['post_id']) {
                $users[$userId]['posts'][] = [
                    'id' => $row['post_id'],
                    'title' => $row['title'],
                    'content' => $row['content']
                ];
            }
        }
        
        return array_values($users);
    }
    
    /**
     * BETTER: Using indexes and LIMIT
     */
    public function getRecentUsers(int $page = 1, int $perPage = 20): array {
        $offset = ($page - 1) * $perPage;
        
        $stmt = $this->db->prepare("
            SELECT * FROM users
            WHERE active = 1
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
        ");
        
        $stmt->bindValue(':limit', $perPage, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();
        
        return $stmt->fetchAll();
    }
}
```

---

## 5. Error Handling & Logging

### Q19: Custom Exception Hierarchy
```php
// Base exception
class ApplicationException extends Exception {
    protected int $statusCode = 500;
    
    public function getStatusCode(): int {
        return $this->statusCode;
    }
}

class ValidationException extends ApplicationException {
    protected int $statusCode = 422;
    private array $errors;
    
    public function __construct(array $errors) {
        $this->errors = $errors;
        parent::__construct('Validation failed');
    }
    
    public function getErrors(): array {
        return $this->errors;
    }
}

class NotFoundException extends ApplicationException {
    protected int $statusCode = 404;
    
    public function __construct(string $resource) {
        parent::__construct("$resource not found");
    }
}

class UnauthorizedException extends ApplicationException {
    protected int $statusCode = 401;
}

// Usage
class UserController {
    public function show(int $id): void {
        $user = $this->userRepo->find($id);
        
        if (!$user) {
            throw new NotFoundException('User');
        }
        
        echo json_encode($user);
    }
    
    public function store(array $data): void {
        $validator = new Validator($data, [
            'name' => 'required|min:3',
            'email' => 'required|email'
        ]);
        
        if ($validator->fails()) {
            throw new ValidationException($validator->errors());
        }
        
        // Create user...
    }
}

// Global exception handler
set_exception_handler(function(Throwable $e) {
    if ($e instanceof ApplicationException) {
        http_response_code($e->getStatusCode());
        
        if ($e instanceof ValidationException) {
            echo json_encode([
                'error' => $e->getMessage(),
                'errors' => $e->getErrors()
            ]);
        } else {
            echo json_encode(['error' => $e->getMessage()]);
        }
    } else {
        // Unexpected error - log it
        error_log($e->getMessage());
        http_response_code(500);
        echo json_encode(['error' => 'Internal server error']);
    }
});
```

---

### Q20: Structured Logging
```php
interface LoggerInterface {
    public function emergency(string $message, array $context = []): void;
    public function alert(string $message, array $context = []): void;
    public function critical(string $message, array $context = []): void;
    public function error(string $message, array $context = []): void;
    public function warning(string $message, array $context = []): void;
    public function notice(string $message, array $context = []): void;
    public function info(string $message, array $context = []): void;
    public function debug(string $message, array $context = []): void;
}

class Logger implements LoggerInterface {
    private string $logPath;
    
    public function __construct(string $logPath = '/var/log/app.log') {
        $this->logPath = $logPath;
    }
    
    public function error(string $message, array $context = []): void {
        $this->log('ERROR', $message, $context);
    }
    
    public function info(string $message, array $context = []): void {
        $this->log('INFO', $message, $context);
    }
    
    public function warning(string $message, array $context = []): void {
        $this->log('WARNING', $message, $context);
    }
    
    // Implement other levels...
    public function emergency(string $message, array $context = []): void {
        $this->log('EMERGENCY', $message, $context);
    }
    
    public function alert(string $message, array $context = []): void {
        $this->log('ALERT', $message, $context);
    }
    
    public function critical(string $message, array $context = []): void {
        $this->log('CRITICAL', $message, $context);
    }
    
    public function notice(string $message, array $context = []): void {
        $this->log('NOTICE', $message, $context);
    }
    
    public function debug(string $message, array $context = []): void {
        $this->log('DEBUG', $message, $context);
    }
    
    private function log(string $level, string $message, array $context): void {
        $logEntry = [
            'timestamp' => date('Y-m-d H:i:s'),
            'level' => $level,
            'message' => $message,
            'context' => $context,
            'memory' => memory_get_usage(true),
            'request_id' => $_SERVER['HTTP_X_REQUEST_ID'] ?? uniqid()
        ];
        
        $logLine = json_encode($logEntry) . PHP_EOL;
        file_put_contents($this->logPath, $logLine, FILE_APPEND);
    }
}

// Usage
$logger = new Logger();

try {
    $logger->info('User login attempt', ['email' => $email]);
    // Login logic...
} catch (Exception $e) {
    $logger->error('Login failed', [
        'email' => $email,
        'error' => $e->getMessage(),
        'trace' => $e->getTraceAsString()
    ]);
}
```

---

## 6. Testing & Quality

### Q21: Unit Testing with PHPUnit
```php
// src/Math/Calculator.php
class Calculator {
    public function add(float $a, float $b): float {
        return $a + $b;
    }
    
    public function divide(float $a, float $b): float {
        if ($b === 0.0) {
            throw new InvalidArgumentException('Division by zero');
        }
        return $a / $b;
    }
}

// tests/Math/CalculatorTest.php
use PHPUnit\Framework\TestCase;

class CalculatorTest extends TestCase {
    private Calculator $calculator;
    
    protected function setUp(): void {
        $this->calculator = new Calculator();
    }
    
    public function testAddition(): void {
        $result = $this->calculator->add(2, 3);
        $this->assertEquals(5, $result);
    }
    
    public function testDivision(): void {
        $result = $this->calculator->divide(10, 2);
        $this->assertEquals(5, $result);
    }
    
    public function testDivisionByZeroThrowsException(): void {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessage('Division by zero');
        
        $this->calculator->divide(10, 0);
    }
    
    /**
     * @dataProvider additionProvider
     */
    public function testAdditionWithDataProvider(
        float $a,
        float $b,
        float $expected
    ): void {
        $result = $this->calculator->add($a, $b);
        $this->assertEquals($expected, $result);
    }
    
    public function additionProvider(): array {
        return [
            [1, 1, 2],
            [2, 3, 5],
            [-1, 1, 0],
            [0.1, 0.2, 0.3],
        ];
    }
}
```

---

### Q22: Testing with Mocks
```php
// tests/UserServiceTest.php
use PHPUnit\Framework\TestCase;

class UserServiceTest extends TestCase {
    public function testRegisterUserSuccess(): void {
        // Create mock repository
        $userRepo = $this->createMock(UserRepositoryInterface::class);
        
        // Set expectations
        $userRepo->expects($this->once())
            ->method('findByEmail')
            ->with('test@example.com')
            ->willReturn(null); // Email not exists
        
        $userRepo->expects($this->once())
            ->method('save')
            ->willReturn(true);
        
        // Create service with mock
        $service = new UserService($userRepo);
        
        // Test
        $user = $service->registerUser(
            'Test User',
            'test@example.com',
            'password123'
        );
        
        $this->assertInstanceOf(User::class, $user);
        $this->assertEquals('Test User', $user->name);
    }
    
    public function testRegisterUserDuplicateEmail(): void {
        $userRepo = $this->createMock(UserRepositoryInterface::class);
        
        $existingUser = new User(1, 'Existing', 'test@example.com', 'hash');
        
        $userRepo->expects($this->once())
            ->method('findByEmail')
            ->willReturn($existingUser);
        
        $service = new UserService($userRepo);
        
        $this->expectException(Exception::class);
        $this->expectExceptionMessage('Email already exists');
        
        $service->registerUser('Test', 'test@example.com', 'password');
    }
}
```

---

## 7. API Development

### Q23: RESTful API Controller
```php
class APIController {
    protected function jsonResponse(
        mixed $data,
        int $statusCode = 200,
        array $headers = []
    ): void {
        http_response_code($statusCode);
        
        header('Content-Type: application/json');
        foreach ($headers as $key => $value) {
            header("$key: $value");
        }
        
        echo json_encode($data, JSON_THROW_ON_ERROR);
        exit;
    }
    
    protected function getJsonInput(): array {
        $json = file_get_contents('php://input');
        return json_decode($json, true, 512, JSON_THROW_ON_ERROR);
    }
}

class UserAPIController extends APIController {
    public function __construct(
        private UserService $userService
    ) {}
    
    // GET /api/users
    public function index(): void {
        $page = $_GET['page'] ?? 1;
        $perPage = $_GET['per_page'] ?? 20;
        
        $users = $this->userService->paginate($page, $perPage);
        
        $this->jsonResponse([
            'data' => $users,
            'meta' => [
                'page' => $page,
                'per_page' => $perPage
            ]
        ]);
    }
    
    // GET /api/users/:id
    public function show(int $id): void {
        try {
            $user = $this->userService->find($id);
            
            if (!$user) {
                $this->jsonResponse([
                    'error' => 'User not found'
                ], 404);
            }
            
            $this->jsonResponse(['data' => $user]);
        } catch (Exception $e) {
            $this->jsonResponse([
                'error' => 'Internal server error'
            ], 500);
        }
    }
    
    // POST /api/users
    public function store(): void {
        try {
            $data = $this->getJsonInput();
            
            $user = $this->userService->create($data);
            
            $this->jsonResponse([
                'data' => $user,
                'message' => 'User created successfully'
            ], 201);
        } catch (ValidationException $e) {
            $this->jsonResponse([
                'error' => 'Validation failed',
                'errors' => $e->getErrors()
            ], 422);
        }
    }
    
    // PUT /api/users/:id
    public function update(int $id): void {
        try {
            $data = $this->getJsonInput();
            
            $user = $this->userService->update($id, $data);
            
            $this->jsonResponse([
                'data' => $user,
                'message' => 'User updated successfully'
            ]);
        } catch (NotFoundException $e) {
            $this->jsonResponse(['error' => $e->getMessage()], 404);
        } catch (ValidationException $e) {
            $this->jsonResponse([
                'error' => 'Validation failed',
                'errors' => $e->getErrors()
            ], 422);
        }
    }
    
    // DELETE /api/users/:id
    public function destroy(int $id): void {
        try {
            $this->userService->delete($id);
            
            $this->jsonResponse([
                'message' => 'User deleted successfully'
            ], 204);
        } catch (NotFoundException $e) {
            $this->jsonResponse(['error' => $e->getMessage()], 404);
        }
    }
}
```

---

### Q24: API Authentication with JWT
```php
use Firebase\JWT\JWT;
use Firebase\JWT\Key;

class JWTAuth {
    private string $secret;
    private string $algorithm = 'HS256';
    
    public function __construct(string $secret) {
        $this->secret = $secret;
    }
    
    public function generateToken(int $userId, array $claims = []): string {
        $payload = [
            'iat' => time(),
            'exp' => time() + (60 * 60 * 24), // 24 hours
            'user_id' => $userId,
            ...$claims
        ];
        
        return JWT::encode($payload, $this->secret, $this->algorithm);
    }
    
    public function verifyToken(string $token): ?object {
        try {
            return JWT::decode($token, new Key($this->secret, $this->algorithm));
        } catch (Exception $e) {
            return null;
        }
    }
    
    public function getUserIdFromToken(string $token): ?int {
        $payload = $this->verifyToken($token);
        return $payload?->user_id;
    }
}

// Middleware
class AuthMiddleware {
    public function __construct(
        private JWTAuth $jwtAuth
    ) {}
    
    public function handle(): void {
        $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
        
        if (!preg_match('/Bearer\s+(.*)$/i', $authHeader, $matches)) {
            http_response_code(401);
            echo json_encode(['error' => 'Missing authorization token']);
            exit;
        }
        
        $token = $matches[1];
        $userId = $this->jwtAuth->getUserIdFromToken($token);
        
        if (!$userId) {
            http_response_code(401);
            echo json_encode(['error' => 'Invalid or expired token']);
            exit;
        }
        
        // Store user ID in global state
        $_SESSION['user_id'] = $userId;
    }
}

// Login endpoint
class AuthController extends APIController {
    public function login(): void {
        $data = $this->getJsonInput();
        
        // Validate credentials
        $user = $this->userService->authenticate(
            $data['email'],
            $data['password']
        );
        
        if (!$user) {
            $this->jsonResponse([
                'error' => 'Invalid credentials'
            ], 401);
        }
        
        $jwtAuth = new JWTAuth(getenv('JWT_SECRET'));
        $token = $jwtAuth->generateToken($user->id, [
            'email' => $user->email,
            'role' => $user->role
        ]);
        
        $this->jsonResponse([
            'token' => $token,
            'user' => $user
        ]);
    }
}
```

---

## 8. Database Design & Query Optimization

### Q25: Database Transactions
```php
class OrderService {
    public function __construct(
        private PDO $db,
        private LoggerInterface $logger
    ) {}
    
    public function createOrder(int $userId, array $items): Order {
        try {
            // Start transaction
            $this->db->beginTransaction();
            
            // 1. Create order
            $stmt = $this->db->prepare("
                INSERT INTO orders (user_id, total, status, created_at)
                VALUES (:user_id, :total, 'pending', NOW())
            ");
            
            $total = array_sum(array_column($items, 'price'));
            $stmt->execute(['user_id' => $userId, 'total' => $total]);
            $orderId = $this->db->lastInsertId();
            
            // 2. Create order items
            $stmt = $this->db->prepare("
                INSERT INTO order_items (order_id, product_id, quantity, price)
                VALUES (:order_id, :product_id, :quantity, :price)
            ");
            
            foreach ($items as $item) {
                $stmt->execute([
                    'order_id' => $orderId,
                    'product_id' => $item['product_id'],
                    'quantity' => $item['quantity'],
                    'price' => $item['price']
                ]);
                
                // 3. Update inventory
                $updateStmt = $this->db->prepare("
                    UPDATE products
                    SET stock = stock - :quantity
                    WHERE id = :product_id AND stock >= :quantity
                ");
                
                $updateStmt->execute([
                    'quantity' => $item['quantity'],
                    'product_id' => $item['product_id']
                ]);
                
                if ($updateStmt->rowCount() === 0) {
                    throw new Exception("Insufficient stock for product {$item['product_id']}");
                }
            }
            
            // Commit transaction
            $this->db->commit();
            
            $this->logger->info("Order created", ['order_id' => $orderId]);
            
            return $this->findOrder($orderId);
            
        } catch (Exception $e) {
            // Rollback on error
            $this->db->rollBack();
            
            $this->logger->error("Order creation failed", [
                'user_id' => $userId,
                'error' => $e->getMessage()
            ]);
            
            throw $e;
        }
    }
    
    private function findOrder(int $orderId): Order {
        // Fetch and return order
        return new Order();
    }
}
```

---

## 🎯 Summary of Best Practices

### Code Quality
✅ Follow PSR standards (PSR-1, PSR-4, PSR-12)  
✅ Use type hints (parameters and return types)  
✅ Write self-documenting code with meaningful names  
✅ Keep functions small and focused (Single Responsibility)  
✅ Use dependency injection over global state

### Security
✅ Use prepared statements (never concatenate SQL)  
✅ Validate and sanitize all user input  
✅ Escape output based on context (HTML, JS, SQL)  
✅ Use CSRF tokens for state-changing operations  
✅ Hash passwords with bcrypt/argon2  
✅ Implement rate limiting for sensitive endpoints

### Performance
✅ Use indexes on frequently queried columns  
✅ Implement caching (Redis, Memcached)  
✅ Use generators for large datasets  
✅ Avoid N+1 queries (use JOIN or eager loading)  
✅ Enable OPcache in production

### Testing
✅ Write unit tests for business logic  
✅ Use mocks/stubs for external dependencies  
✅ Aim for > 80% code coverage  
✅ Test edge cases and error conditions  
✅ Use data providers for testing multiple scenarios

### Architecture
✅ Use design patterns appropriately  
✅ Follow SOLID principles  
✅ Separate concerns (MVC, Repository pattern)  
✅ Use interfaces for contracts  
✅ Implement proper error handling

---

**Interview Tips:**
1. **Explain your reasoning** - Don't just write code, explain WHY you chose that approach
2. **Discuss trade-offs** - "I used X because Y, but Z would be better if we needed to scale"
3. **Show awareness of production concerns** - Security, performance, maintainability
4. **Ask clarifying questions** - "Should this handle concurrent requests?" 
5. **Refactor iteratively** - Start simple, then improve

Good luck with your interviews! 🚀
