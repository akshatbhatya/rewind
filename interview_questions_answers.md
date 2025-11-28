# 🎯 Complete Backend Developer Interview Guide (3 Years Experience)

**Technologies Covered:** PHP, Laravel, WordPress, MySQL, JavaScript  
**Experience Level:** 3 Years (Mid-Level Backend Developer)

This comprehensive guide contains **55+ real-world interview questions** with detailed answers. Questions are categorized by technology and difficulty, covering both theoretical concepts and practical scenarios you'll face in actual interviews.

---

## Table of Contents
1. [PHP Core & OOP (17 Questions)](#1-php-core--oop)
2. [Laravel Framework (15 Questions)](#2-laravel-framework)
3. [MySQL & Database (12 Questions)](#3-mysql--database)
4. [WordPress Development (8 Questions)](#4-wordpress-development)
5. [JavaScript (7 Questions)](#5-javascript-backend-context)
6. [System Design & Architecture (6 Questions)](#6-system-design--architecture)
7. [Coding & Problem Solving (5 Questions)](#7-coding--problem-solving)

---

## 1. PHP Core & OOP

### Q1: Explain the difference between `Interface`, `Abstract Class`, and `Trait`. When would you use each?
**Answer:**

| Feature | Interface | Abstract Class | Trait |
|---------|-----------|----------------|-------|
| **Instantiation** | Cannot be instantiated | Cannot be instantiated | Cannot be instantiated |
| **Methods** | Only method signatures | Abstract + concrete methods | Only concrete methods |
| **Properties** | Constants only | Can have properties | Can have properties |
| **Inheritance** | Multiple allowed | Single only | Multiple allowed |

**When to use:**
*   **Interface**: Contract across unrelated classes (e.g., `Loggable`, `Cacheable`, `Payable`). Multiple classes implement the same behavior.
*   **Abstract Class**: Base class with shared logic (e.g., `Animal` → `Dog`, `Cat`). Child classes share identity + implementation.
*   **Trait**: Horizontal code reuse (e.g., `HasSlug`, `SoftDeletes`). Avoid duplication across independent classes.

```php
// Interface - Contract
interface PaymentGateway {
    public function charge(float $amount);
}

// Abstract Class - Shared logic
abstract class Vehicle {
    protected $fuel;
    abstract public function start();
    public function refuel() { /* shared logic */ }
}

// Trait - Code reuse
trait Timestampable {
    public function touch() { $this->updated_at = now(); }
}
```

---

### Q2: How does PHP Garbage Collection (GC) work? What are Reference Cycles?
**Answer:**

PHP uses **reference counting**. Each variable has a `refcount` that tracks how many symbols point to it. When `refcount` reaches 0, memory is freed immediately.

**Reference Cycle Problem:**
```php
class Node {
    public $next = null;
}
$a = new Node(); // refcount = 1
$b = new Node(); // refcount = 1
$a->next = $b;   // $b refcount = 2
$b->next = $a;   // $a refcount = 2
unset($a, $b);   // Both refcount = 1 (still referenced by each other!)
```

**Solution:** PHP 5.3+ introduced a **cyclic GC** that periodically detects and cleans these cycles. For long-running scripts (workers, daemons), you might manually call `gc_collect_cycles()`.

---

### Q3: What are the key differences between PHP 7 and PHP 8?
**Answer:**

| Feature | PHP 7 | PHP 8 |
|---------|-------|-------|
| **JIT Compiler** | ❌ | ✅ (20-30% faster for CPU-heavy tasks) |
| **Union Types** | ❌ | `function(int\|float $x)` |
| **Named Arguments** | ❌ | `foo(b: 2, a: 1)` |
| **Attributes** | PHPDoc only | `#[Route('/api')]` |
| **Match Expression** | ❌ | Strict `match()` vs loose `switch` |
| **Nullsafe Operator** | ❌ | `$user?->profile?->name` |
| **Constructor Promotion** | Manual assignment | `public function __construct(public string $name)` |

**Example - Constructor Promotion:**
```php
// PHP 7
class User {
    public $name;
    public $email;
    public function __construct($name, $email) {
        $this->name = $name;
        $this->email = $email;
    }
}

// PHP 8
class User {
    public function __construct(
        public string $name,
        public string $email
    ) {}
}
```

---

### Q4: Explain `include` vs `require` vs `include_once` vs `require_once`.
**Answer:**

| | `include` | `require` | `_once` variant |
|-|-----------|-----------|-----------------|
| **If file missing** | Warning (continues) | Fatal error (stops) | Same as base |
| **Multiple calls** | Executes every time | Executes every time | Only once |

**Use case:**
*   `require_once`: Config files, class definitions.
*   `include`: Optional templates (fallback if missing).

---

### Q5: What are PHP Magic Methods? Name 5 commonly used ones.
**Answer:**

1.  **`__construct()`**: Called when creating an object.
2.  **`__destruct()`**: Called when object is destroyed (cleanup).
3.  **`__get($name)`**: Access inaccessible/non-existent properties.
4.  **`__set($name, $value)`**: Set inaccessible/non-existent properties.
5.  **`__call($method, $args)`**: Call inaccessible methods.
6.  **`__toString()`**: Object to string conversion.
7.  **`__invoke()`**: Treat object as a function.

```php
class User {
    private $data = [];
    
    public function __get($key) {
        return $this->data[$key] ?? null;
    }
    
    public function __set($key, $value) {
        $this->data[$key] = $value;
    }
    
    public function __toString() {
        return $this->data['name'] ?? 'Guest';
    }
}
```

---

### Q6: What is the difference between `==` and `===`?
**Answer:**

*   **`==` (Loose comparison)**: Compares values after type juggling.
*   **`===` (Strict comparison)**: Compares values AND types.

```php
0 == false    // true
0 === false   // false

'1' == 1      // true
'1' === 1     // false

null == 0     // true
null === 0    // false
```

**Best Practice:** Always use `===` unless you explicitly need type coercion.

---

### Q7: Explain `static` vs `self` vs `parent`.
**Answer:**

*   **`self`**: Refers to the class where it's written (early binding).
*   **`static`**: Refers to the called class (late static binding, PHP 5.3+).
*   **`parent`**: Refers to the parent class.

```php
class A {
    public static function who() { return 'A'; }
    public static function testSelf() { return self::who(); }
    public static function testStatic() { return static::who(); }
}

class B extends A {
    public static function who() { return 'B'; }
}

echo B::testSelf();   // "A" (self points to A)
echo B::testStatic(); // "B" (static points to B)
```

---

### Q8: What is `yield` and Generators?
**Answer:**

Generators allow you to iterate over data without building an array in memory. They use `yield` to return values one at a time.

```php
// Without Generator (loads all in memory)
function getNums() {
    $result = [];
    for ($i = 1; $i <= 1000000; $i++) {
        $result[] = $i;
    }
    return $result;
}

// With Generator (memory efficient)
function getNums() {
    for ($i = 1; $i <= 1000000; $i++) {
        yield $i;
    }
}

foreach (getNums() as $num) {
    echo $num; // Only one number in memory at a time
}
```

---

### Q9: What are SPL (Standard PHP Library) Data Structures?
**Answer:**

SPL provides data structures and iterators:
*   **`SplStack`**: LIFO stack.
*   **`SplQueue`**: FIFO queue.
*   **`SplHeap`**: Priority queue.
*   **`SplDoublyLinkedList`**: Doubly linked list.

```php
$stack = new SplStack();
$stack->push('first');
$stack->push('second');
echo $stack->pop(); // "second"
```

---

### Q10: Explain `Closure` and `use` keyword.
**Answer:**

A **Closure** is an anonymous function that can capture variables from its parent scope using `use`.

```php
$tax = 0.15;
$calculateTotal = function($price) use ($tax) {
    return $price + ($price * $tax);
};
echo $calculateTotal(100); // 115
```

**Pass by reference:**
```php
$counter = 0;
$increment = function() use (&$counter) {
    $counter++;
};
$increment();
echo $counter; // 1
```

---

### Q11: What is `Reflection` in PHP?
**Answer:**

Reflection allows you to inspect classes, methods, and properties at runtime.

```php
$reflection = new ReflectionClass('User');
echo $reflection->getName();
foreach ($reflection->getMethods() as $method) {
    echo $method->name;
}
```

**Use cases:** Building ORMs, Dependency Injection containers, testing frameworks.

---

### Q12: Explain PSR Standards (PSR-1, PSR-4, PSR-12).
**Answer:**

*   **PSR-1**: Basic coding standards (class names, method visibility).
*   **PSR-4**: Autoloading standard (namespace → file path mapping).
*   **PSR-12**: Extended coding style guide (indentation, braces).

```php
// PSR-4 Autoloading
// App\Controllers\UserController → app/Controllers/UserController.php
```

---

### Q13: What is the difference between `final`, `const`, and `static`?
**Answer:**

*   **`final`**: Prevents class inheritance or method overriding.
*   **`const`**: Compile-time constant (belongs to class).
*   **`static`**: Belongs to class, not instance.

```php
class Config {
    const API_KEY = 'abc123'; // Constant
    public static $cache = []; // Static property
    
    final public function critical() {} // Cannot override
}
```

---

### Q14: How do you handle errors in PHP 7+?
**Answer:**

PHP 7+ uses **Exceptions** and **Throwable** interface.

```php
try {
    // Code
} catch (TypeError $e) {
    // Type error
} catch (DivisionByZeroError $e) {
    // Division by zero
} catch (Throwable $e) {
    // Catch all
} finally {
    // Always runs
}
```

---

### Q15: What is `Composer` and `autoload`?
**Answer:**

**Composer** is a dependency manager for PHP. It generates an autoloader that loads classes automatically.

```json
{
    "autoload": {
        "psr-4": {
            "App\\": "src/"
        }
    }
}
```

After `composer dump-autoload`, classes under `src/` are auto-loaded.

---

### Q16: Explain `session_start()` and session security.
**Answer:**

Sessions store data server-side. `session_start()` initializes/resumes a session.

**Security best practices:**
*   Use `session_regenerate_id()` after login to prevent session fixation.
*   Set `httponly` and `secure` flags in `php.ini`.
*   Store sensitive data encrypted.

```php
session_start();
$_SESSION['user_id'] = 123;
session_regenerate_id(true); // Regenerate after login
```

---

### Q17: What is the difference between `Cookie` and `Session`?
**Answer:**

| Feature | Cookie | Session |
|---------|--------|---------|
| **Storage** | Client-side (browser) | Server-side |
| **Size** | ~4KB | Unlimited (server limit) |
| **Security** | Less secure (accessible via JS) | More secure |
| **Lifetime** | Set expiration | Until browser closes (or timeout) |

---

## 2. Laravel Framework

### Q18: Explain the Laravel Request Lifecycle in detail.
**Answer:**

1.  **Entry Point**: `public/index.php` → loads Composer autoloader.
2.  **Kernel**: Request sent to HTTP Kernel (`app/Http/Kernel.php`).
3.  **Service Providers**: Bootstrap app services (`AppServiceProvider`, etc.).
4.  **Middleware (Global)**: CSRF, sessions, maintenance mode.
5.  **Router**: Matches URL to route (`routes/web.php` or `api.php`).
6.  **Middleware (Route)**: Auth, throttle, custom middleware.
7.  **Controller**: Controller method executes.
8.  **Response**: View/JSON returned, passes back through middleware.
9.  **Terminate**: Cleanup tasks (if any).

---

### Q19: What is the Service Container and how does Dependency Injection work?
**Answer:**

The **Service Container** is Laravel's IoC (Inversion of Control) container. It manages class dependencies and performs dependency injection.

**Binding:**
```php
// In a Service Provider
$this->app->bind(PaymentGateway::class, StripeGateway::class);
```

**Resolving:**
```php
// Laravel auto-injects
public function __construct(PaymentGateway $gateway) {
    $this->gateway = $gateway;
}
```

**Benefits:** Testable, loosely coupled, swappable implementations.

---

### Q20: What is the N+1 Query Problem and how do you solve it?
**Answer:**

**Problem:**
```php
$posts = Post::all(); // 1 query
foreach ($posts as $post) {
    echo $post->author->name; // N queries (one per post)
}
```

**Solution - Eager Loading:**
```php
$posts = Post::with('author')->get(); // 2 queries total
```

**For nested relations:**
```php
$posts = Post::with('author.profile')->get();
```

---

### Q21: Explain Laravel Queues. How do they work?
**Answer:**

Queues defer time-consuming tasks (emails, uploads) to background workers.

**Creating a Job:**
```php
php artisan make:job SendWelcomeEmail
```

**Dispatching:**
```php
SendWelcomeEmail::dispatch($user);
```

**Processing:**
```php
php artisan queue:work
```

**Drivers:** Database, Redis, SQS, Beanstalkd.  
**Laravel Horizon:** Dashboard for Redis queues.

---

### Q22: What are Service Providers and when do you create one?
**Answer:**

Service Providers are the central place to bootstrap services (bind to container, register routes, etc.).

**When to create:**
*   Third-party package integration.
*   Custom services that need configuration.

```php
class PaymentServiceProvider extends ServiceProvider {
    public function register() {
        $this->app->bind(PaymentGateway::class, StripeGateway::class);
    }
    
    public function boot() {
        // Register routes, publish config, etc.
    }
}
```

---

### Q23: Explain Middleware. Give 3 real-world examples.
**Answer:**

Middleware acts as a filter for HTTP requests.

**Examples:**
1.  **Authentication**: Ensure user is logged in.
2.  **CORS**: Add cross-origin headers.
3.  **Logging**: Log all incoming requests.

```php
public function handle($request, Closure $next) {
    if (!auth()->check()) {
        return redirect('login');
    }
    return $next($request);
}
```

---

### Q24: What is Eloquent ORM? Explain relationships.
**Answer:**

Eloquent is Laravel's Active Record ORM.

**Relationships:**
*   **`hasOne`**: User → Profile
*   **`hasMany`**: Post → Comments
*   **`belongsTo`**: Comment → Post
*   **`belongsToMany`**: Users ↔ Roles (pivot table)

```php
class User extends Model {
    public function posts() {
        return $this->hasMany(Post::class);
    }
}
```

---

### Q25: What are Accessors and Mutators?
**Answer:**

*   **Accessor**: Modify data when reading from model.
*   **Mutator**: Modify data before saving to DB.

```php
// Accessor
public function getNameAttribute($value) {
    return ucfirst($value);
}

// Mutator
public function setPasswordAttribute($value) {
    $this->attributes['password'] = bcrypt($value);
}
```

---

### Q26: What is `php artisan tinker`?
**Answer:**

**Tinker** is a REPL (Read-Eval-Print Loop) for Laravel. It lets you interact with your app in a terminal.

```bash
php artisan tinker
>>> User::count()
=> 42
>>> $user = User::find(1)
>>> $user->email
```

---

### Q27: Explain Laravel Events and Listeners.
**Answer:**

Events allow you to decouple code. When something happens (event), multiple listeners can respond.

**Example:**
```php
// Event
class OrderPlaced {
    public function __construct(public Order $order) {}
}

// Listener
class SendOrderConfirmation {
    public function handle(OrderPlaced $event) {
        Mail::to($event->order->user)->send(new OrderEmail());
    }
}

// Dispatching
event(new OrderPlaced($order));
```

---

### Q28: What is Laravel Sanctum vs Passport?
**Answer:**

| Feature | Sanctum | Passport |
|---------|---------|----------|
| **Use case** | SPA, mobile apps (simple) | Full OAuth2 server |
| **Tokens** | Lightweight | OAuth2 |
| **Complexity** | Low | High |

**Sanctum:** Token-based auth for SPAs.  
**Passport:** Full OAuth2 implementation (third-party apps).

---

### Q29: What are Laravel Gates and Policies?
**Answer:**

Authorization tools.

*   **Gates**: Simple closures for authorization checks.
*   **Policies**: Class-based authorization for models.

```php
// Gate
Gate::define('update-post', fn($user, $post) => $user->id === $post->user_id);

// Policy
public function update(User $user, Post $post) {
    return $user->id === $post->user_id;
}

// Usage
if ($user->can('update', $post)) { }
```

---

### Q30: Explain `dd()`, `dump()`, and Laravel Debugbar.
**Answer:**

*   **`dd($var)`**: Die and dump (var_dump + exit).
*   **`dump($var)`**: Dump without stopping.
*   **Laravel Debugbar**: UI panel with queries, routes, views.

---

### Q31: What is Route Model Binding?
**Answer:**

Automatically inject model instances into routes based on ID.

```php
// routes/web.php
Route::get('/posts/{post}', function (Post $post) {
    return $post; // Laravel auto-fetches by ID
});
```

**Custom key:**
```php
Route::get('/posts/{post:slug}', function (Post $post) { });
```

---

### Q32: What are Laravel Collections? Give 3 useful methods.
**Answer:**

Collections are enhanced arrays with helper methods.

```php
$collection = collect([1, 2, 3, 4]);

$collection->map(fn($n) => $n * 2); // [2, 4, 6, 8]
$collection->filter(fn($n) => $n > 2); // [3, 4]
$collection->sum(); // 10
```

**Other useful methods:** `pluck`, `groupBy`, `chunk`, `reduce`.

---

---

## 3. MySQL & Database

### Q33: Explain Database Indexing. When should you NOT use an index?
**Answer:**

An **index** is a data structure (B-Tree) that improves SELECT performance but slows down INSERT/UPDATE/DELETE.

**When NOT to index:**
*   Small tables (< 1000 rows).
*   Columns with low cardinality (e.g., `gender` with 2 values).
*   Columns updated frequently.

**Types:**
*   **Primary**: Unique, non-null.
*   **Unique**: Unique values.
*   **Composite**: Multiple columns `INDEX(user_id, created_at)`.

---

### Q34: What are ACID properties?
**Answer:**

*   **Atomicity**: All or nothing (transaction commits or rolls back completely).
*   **Consistency**: DB moves from one valid state to another.
*   **Isolation**: Concurrent transactions don't interfere.
*   **Durability**: Committed data survives crashes.

---

### Q35: Explain `INNER JOIN`, `LEFT JOIN`, `RIGHT JOIN`, `FULL OUTER JOIN`.
**Answer:**

```sql
-- INNER JOIN: Only matching rows
SELECT * FROM users INNER JOIN orders ON users.id = orders.user_id;

-- LEFT JOIN: All from left + matches from right
SELECT * FROM users LEFT JOIN orders ON users.id = orders.user_id;

-- RIGHT JOIN: All from right + matches from left
SELECT * FROM orders RIGHT JOIN users ON users.id = orders.user_id;

-- FULL OUTER JOIN: All from both (MySQL doesn't support, use UNION)
```

---

### Q36: What is Query Optimization? Give 3 techniques.
**Answer:**

1.  **Use indexes** on WHERE/JOIN columns.
2.  **Avoid SELECT \***: Only select needed columns.
3.  **Use EXPLAIN**: Analyze query execution.

```sql
EXPLAIN SELECT * FROM users WHERE email = 'test@example.com';
```

---

### Q37: What are Database Transactions?
**Answer:**

Transactions group multiple queries into a single unit.

```sql
START TRANSACTION;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT; -- or ROLLBACK on error
```

**In Laravel:**
```php
DB::transaction(function () {
    DB::table('users')->update(['votes' => 1]);
    DB::table('posts')->delete();
});
```

---

### Q38: What is Database Normalization?
**Answer:**

Process of organizing data to reduce redundancy.

*   **1NF**: Atomic values (no arrays in cells).
*   **2NF**: No partial dependencies (all non-key columns depend on the full primary key).
*   **3NF**: No transitive dependencies (non-key columns don't depend on other non-key columns).

**Denormalization**: Sometimes break rules for performance (e.g., store computed values).

---

### Q39: What is the difference between `UNION` and `UNION ALL`?
**Answer:**

*   **`UNION`**: Combines results and removes duplicates.
*   **`UNION ALL`**: Combines results and keeps duplicates (faster).

```sql
SELECT name FROM users
UNION
SELECT name FROM customers;
```

---

### Q40: Explain Subqueries vs Joins.
**Answer:**

*   **Subquery**: Nested SELECT.
*   **Join**: Combine tables.

```sql
-- Subquery
SELECT * FROM users WHERE id IN (SELECT user_id FROM orders);

-- Join (usually faster)
SELECT users.* FROM users INNER JOIN orders ON users.id = orders.user_id;
```

---

### Q41: What is a Stored Procedure?
**Answer:**

Precompiled SQL code stored in the database.

```sql
DELIMITER $$
CREATE PROCEDURE GetUser(IN userId INT)
BEGIN
    SELECT * FROM users WHERE id = userId;
END $$
DELIMITER ;

CALL GetUser(1);
```

**Pros:** Performance, reusability.  
**Cons:** Hard to version control, debug.

---

### Q42: What are Database Triggers?
**Answer:**

Automatic actions when INSERT/UPDATE/DELETE occurs.

```sql
CREATE TRIGGER after_user_insert
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action) VALUES ('User created');
END;
```

---

### Q43: Explain `COUNT(*)` vs `COUNT(column)`.
**Answer:**

*   **`COUNT(*)`**: Counts all rows (including NULL).
*   **`COUNT(column)`**: Counts non-NULL values in that column.

---

### Q44: What is Database Connection Pooling?
**Answer:**

Reuse database connections instead of opening/closing for each request. Reduces overhead.

**Laravel**: Uses persistent connections via PDO.

---

## 4. WordPress Development

### Q45: Explain WordPress Hooks: `actions` vs `filters`.
**Answer:**

*   **Actions (`do_action`)**: Execute code at a specific point. No return value.
*   **Filters (`apply_filters`)**: Modify data. Must return value.

```php
// Action - Send email on publish
add_action('publish_post', function($post_id) {
    wp_mail('admin@site.com', 'New post', 'Post published');
});

// Filter - Modify excerpt length
add_filter('excerpt_length', function($length) {
    return 50;
});
```

---

### Q46: How do you secure a WordPress plugin/theme?
**Answer:**

1.  **Sanitize Input**: `sanitize_text_field()`, `absint()`, `sanitize_email()`.
2.  **Escape Output**: `esc_html()`, `esc_url()`, `esc_attr()`.
3.  **Nonces (CSRF)**: `wp_create_nonce()`, `wp_verify_nonce()`.
4.  **Permissions**: `current_user_can('manage_options')`.
5.  **Prepared Statements**: Use `$wpdb->prepare()` for SQL.

```php
// Nonce example
$nonce = wp_create_nonce('my_action');
if (wp_verify_nonce($_POST['_wpnonce'], 'my_action')) {
    // Process
}
```

---

### Q47: What is `WP_Query` vs `get_posts()` vs `query_posts()`?
**Answer:**

*   **`WP_Query`**: Full control, custom loops.
*   **`get_posts()`**: Simple array of posts (uses `WP_Query` internally).
*   **`query_posts()`**: DEPRECATED (modifies main query, avoid).

```php
// WP_Query
$query = new WP_Query(['post_type' => 'product']);
while ($query->have_posts()) {
    $query->the_post();
    the_title();
}
wp_reset_postdata();

// get_posts
$posts = get_posts(['numberposts' => 5]);
```

---

### Q48: Explain WordPress Custom Post Types.
**Answer:**

Custom Post Types allow you to create content types beyond posts/pages.

```php
register_post_type('product', [
    'labels' => ['name' => 'Products'],
    'public' => true,
    'supports' => ['title', 'editor', 'thumbnail'],
]);
```

---

### Q49: What is `$wpdb` and when do you use it?
**Answer:**

`$wpdb` is WordPress's database class for custom queries.

```php
global $wpdb;
$results = $wpdb->get_results("SELECT * FROM {$wpdb->prefix}custom_table");
```

**Use for:** Custom tables, complex queries not supported by `WP_Query`.

---

### Q50: Explain WordPress Transients API.
**Answer:**

Transients are cached data with an expiration time.

```php
// Set
set_transient('my_data', $data, 3600); // 1 hour

// Get
$data = get_transient('my_data');
if (false === $data) {
    // Fetch fresh data
}
```

---

### Q51: What is the WordPress REST API?
**Answer:**

Built-in REST API for accessing WP data via HTTP.

**Endpoints:**
*   `/wp-json/wp/v2/posts` (GET posts)
*   `/wp-json/wp/v2/users` (GET users)

**Custom endpoint:**
```php
register_rest_route('myplugin/v1', '/products', [
    'methods' => 'GET',
    'callback' => 'get_products',
]);
```

---

### Q52: Explain `wp_enqueue_script()` vs `wp_register_script()`.
**Answer:**

*   **`wp_register_script()`**: Register a script (doesn't load it).
*   **`wp_enqueue_script()`**: Load a script (and register if not already).

```php
wp_enqueue_script('my-script', get_template_directory_uri() . '/js/script.js', ['jquery'], '1.0', true);
```

---

## 5. JavaScript (Backend Context)

### Q53: Explain `Promises` vs `Async/Await`.
**Answer:**

**Promises:**
```javascript
fetchData()
    .then(data => console.log(data))
    .catch(err => console.error(err));
```

**Async/Await (cleaner):**
```javascript
try {
    const data = await fetchData();
    console.log(data);
} catch (err) {
    console.error(err);
}
```

---

### Q54: What is the Event Loop?
**Answer:**

JavaScript is single-threaded. The **Event Loop** allows async operations by:
1.  Executing code in the **Call Stack**.
2.  Async tasks (timers, HTTP) are handled by **Web APIs**.
3.  Completed tasks go to the **Callback Queue**.
4.  When the Call Stack is empty, the Event Loop pushes tasks from the queue.

---

### Q55: Explain `let`, `const`, and `var`.
**Answer:**

| | `var` | `let` | `const` |
|-|-------|-------|---------|
| **Scope** | Function | Block | Block |
| **Reassignable** | Yes | Yes | No |
| **Hoisting** | Yes (undefined) | Yes (TDZ) | Yes (TDZ) |

**Temporal Dead Zone (TDZ):** `let`/`const` are hoisted but not initialized.

---

### Q56: What is `this` keyword?
**Answer:**

*   **Global**: `this = window` (browser) or `global` (Node).
*   **Object method**: `this = object`.
*   **Arrow function**: `this = lexical parent`.

```javascript
const obj = {
    name: 'Test',
    greet: function() { console.log(this.name); },
    greetArrow: () => { console.log(this.name); }
};
obj.greet(); // "Test"
obj.greetArrow(); // undefined (lexical this)
```

---

### Q57: Explain Closures.
**Answer:**

A closure is a function that remembers its outer variables.

```javascript
function outer() {
    let count = 0;
    return function inner() {
        count++;
        console.log(count);
    };
}
const counter = outer();
counter(); // 1
counter(); // 2
```

---

### Q58: What is Hoisting?
**Answer:**

Variables and functions are moved to the top of their scope during compilation.

```javascript
console.log(x); // undefined
var x = 5;

// Interpreted as:
var x;
console.log(x);
x = 5;
```

---

### Q59: Explain `map()`, `filter()`, `reduce()`.
**Answer:**

```javascript
const nums = [1, 2, 3, 4];

// map: Transform each item
nums.map(n => n * 2); // [2, 4, 6, 8]

// filter: Select items
nums.filter(n => n > 2); // [3, 4]

// reduce: Aggregate to single value
nums.reduce((sum, n) => sum + n, 0); // 10
```

---

## 6. System Design & Architecture

### Q60: Design a URL Shortener (like Bit.ly).
**Answer:**

**Requirements:**
*   Shorten long URLs.
*   Redirect short URLs.

**Design:**
*   **Database**: `id (auto-inc)`, `long_url`, `short_code`, `created_at`.
*   **Algorithm**: Convert ID to Base62 (a-z, A-Z, 0-9).
    *   ID 1 → 'b'
    *   ID 1000 → 'qi'
*   **Caching**: Redis for `short_code` → `long_url` lookup (read-heavy).
*   **Scaling**: Horizontal scaling, CDN for static redirects.

---

### Q61: How would you design a Rate Limiter?
**Answer:**

**Algorithms:**
*   **Token Bucket**: Refill tokens at a fixed rate, consume on requests.
*   **Sliding Window**: Track requests in time windows.

**Implementation (Redis):**
```php
$key = "rate_limit:user_$userId";
$count = Redis::incr($key);
if ($count === 1) {
    Redis::expire($key, 60); // 60 seconds
}
if ($count > 100) {
    throw new RateLimitExceeded();
}
```

---

### Q62: Explain Caching Strategies.
**Answer:**

*   **Cache-Aside**: App checks cache, if miss → fetch from DB, store in cache.
*   **Write-Through**: Write to cache and DB simultaneously.
*   **Write-Back**: Write to cache, async write to DB.

**Tools**: Redis, Memcached, CDN.

---

### Q63: What is Load Balancing?
**Answer:**

Distributes traffic across multiple servers.

**Algorithms:**
*   **Round Robin**: Requests distributed sequentially.
*   **Least Connections**: Send to server with fewest connections.
*   **IP Hash**: Route based on client IP.

**Tools:** Nginx, HAProxy, AWS ELB.

---

### Q64: Explain Microservices vs Monolith.
**Answer:**

| Feature | Monolith | Microservices |
|---------|----------|---------------|
| **Deployment** | Single unit | Independent services |
| **Scaling** | Scale entire app | Scale individual services |
| **Complexity** | Lower | Higher |
| **Tech Stack** | Uniform | Polyglot |

---

### Q65: What is CAP Theorem?
**Answer:**

In distributed systems, you can only guarantee **2 of 3**:
*   **Consistency**: All nodes see the same data.
*   **Availability**: Every request gets a response.
*   **Partition Tolerance**: System works despite network failures.

**Example:**
*   **CA**: Traditional RDBMS (MySQL).
*   **CP**: MongoDB (consistency > availability).
*   **AP**: Cassandra (availability > consistency).

---

## 7. Coding & Problem Solving

### Q66: Write a function to find duplicates in an array.
**Answer:**

```php
function findDuplicates($arr) {
    $seen = [];
    $duplicates = [];
    foreach ($arr as $item) {
        if (isset($seen[$item])) {
            $duplicates[] = $item;
        } else {
            $seen[$item] = true;
        }
    }
    return array_unique($duplicates);
}
```

---

### Q67: Reverse a string without using built-in functions.
**Answer:**

```php
function reverseString($str) {
    $reversed = '';
    for ($i = strlen($str) - 1; $i >= 0; $i--) {
        $reversed .= $str[$i];
    }
    return $reversed;
}
```

---

### Q68: Check if a string is a palindrome.
**Answer:**

```php
function isPalindrome($str) {
    $str = strtolower(preg_replace('/[^a-z0-9]/', '', $str));
    return $str === strrev($str);
}
```

---

### Q69: Write FizzBuzz.
**Answer:**

```php
for ($i = 1; $i <= 100; $i++) {
    if ($i % 15 === 0) echo "FizzBuzz\n";
    elseif ($i % 3 === 0) echo "Fizz\n";
    elseif ($i % 5 === 0) echo "Buzz\n";
    else echo "$i\n";
}
```

---

### Q70: Find the factorial of a number (recursive).
**Answer:**

```php
function factorial($n) {
    return $n <= 1 ? 1 : $n * factorial($n - 1);
}
```

---

## 🎓 Study Tips

1.  **Practice Coding**: LeetCode Easy/Medium (2-3 problems daily).
2.  **System Design**: Read "Designing Data-Intensive Applications".
3.  **Mock Interviews**: Use Pramp or interviewing.io.
4.  **Review Your Projects**: Be ready to discuss architecture decisions.
5.  **Stay Updated**: Follow Laravel News, PHP Weekly.

---

**Good luck with your interviews! 🚀**
