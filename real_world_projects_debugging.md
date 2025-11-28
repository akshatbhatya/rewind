# 🏆 Senior Backend Developer: Real-World Projects & Debugging Guide

**Advanced interview questions based on actual industry scenarios + Fast debugging techniques**

This guide focuses on **real-world project challenges** you'll face in production environments and **proven debugging strategies** to solve issues quickly.

---

## 📋 Table of Contents

1. [Real-World Project Scenarios](#real-world-project-scenarios)
2. [Fast Debugging Techniques](#fast-debugging-techniques)
3. [Production Issues & Solutions](#production-issues--solutions)
4. [Performance Debugging](#performance-debugging)
5. [Security Incident Response](#security-incident-response)

---

## 🎯 Real-World Project Scenarios

### Scenario 1: E-Commerce Platform - High Traffic Sale Event

**Context:** You're building a flash sale feature for an e-commerce platform expecting 50,000+ concurrent users.

**Challenge Questions:**

**Q1: How would you handle inventory management to prevent overselling?**

```php
// Problem: Race condition in inventory check
// Bad approach
public function purchaseProduct($productId, $quantity)
{
    $product = Product::find($productId);
    
    if ($product->stock >= $quantity) {
        // Race condition here! Another request might check at same time
        $product->stock -= $quantity;
        $product->save();
        
        Order::create([...]);
    }
}

// Solution 1: Database-level locking
public function purchaseProduct($productId, $quantity)
{
    DB::transaction(function () use ($productId, $quantity) {
        $product = Product::where('id', $productId)
            ->lockForUpdate()
            ->first();
        
        if ($product->stock < $quantity) {
            throw new InsufficientStockException();
        }
        
        // Atomic decrement
        $affected = Product::where('id', $productId)
            ->where('stock', '>=', $quantity)
            ->decrement('stock', $quantity);
        
        if ($affected === 0) {
            throw new InsufficientStockException();
        }
        
        Order::create([...]);
    });
}

// Solution 2: Redis-based inventory with Lua script (faster)
public function purchaseProduct($productId, $quantity)
{
    $lua = <<<LUA
        local stock = redis.call('GET', KEYS[1])
        if tonumber(stock) >= tonumber(ARGV[1]) then
            redis.call('DECRBY', KEYS[1], ARGV[1])
            return 1
        else
            return 0
        end
    LUA;
    
    $result = Redis::eval($lua, 1, "product:$productId:stock", $quantity);
    
    if ($result == 0) {
        throw new InsufficientStockException();
    }
    
    // Async process order
    ProcessOrderJob::dispatch($productId, $quantity);
}
```

**Q2: How would you handle the sudden traffic spike?**

```php
// Architecture approach:
// 1. Queue-based order processing
// 2. CDN for static assets
// 3. Redis for session/cache
// 4. Database read replicas
// 5. Horizontal scaling with load balancer

// Implementation: Queue everything
Route::post('/purchase', function (Request $request) {
    // Validate immediately
    $validated = $request->validate([
        'product_id' => 'required|exists:products,id',
        'quantity' => 'required|integer|min:1',
    ]);
    
    // Check Redis inventory (fast)
    $available = Redis::get("product:{$validated['product_id']}:stock");
    
    if ($available < $validated['quantity']) {
        return response()->json(['error' => 'Out of stock'], 400);
    }
    
    // Reserve inventory immediately
    Redis::decrby("product:{$validated['product_id']}:stock", $validated['quantity']);
    
    // Queue the actual order processing
    ProcessFlashSaleOrder::dispatch($validated)
        ->onQueue('high-priority');
    
    return response()->json([
        'message' => 'Order is being processed',
        'order_id' => Str::uuid()
    ], 202); // Accepted
});

// Rate limiting per user
Route::middleware('throttle:10,1')->group(function () {
    Route::post('/purchase', [PurchaseController::class, 'store']);
});
```

---

### Scenario 2: Social Media Platform - Real-time Notifications

**Context:** Building a notification system for a social platform with millions of users.

**Q3: How would you design a scalable notification system?**

```php
// Architecture:
// 1. Event-driven with Laravel Events
// 2. Queue-based delivery
// 3. WebSocket for real-time (Laravel Echo + Pusher/Socket.io)
// 4. Database for persistence
// 5. Redis for unread counts

// Event
class PostLiked implements ShouldBroadcast
{
    use Dispatchable, InteractsWithSockets, SerializesModels;
    
    public function __construct(
        public Post $post,
        public User $likedBy
    ) {}
    
    public function broadcastOn(): array
    {
        return [
            new PrivateChannel('user.' . $this->post->user_id),
        ];
    }
    
    public function broadcastWith(): array
    {
        return [
            'message' => "{$this->likedBy->name} liked your post",
            'post_id' => $this->post->id,
            'timestamp' => now()->toISOString(),
        ];
    }
}

// Listener - Store in database
class StoreNotification
{
    public function handle(PostLiked $event): void
    {
        Notification::create([
            'user_id' => $event->post->user_id,
            'type' => 'post_liked',
            'data' => [
                'post_id' => $event->post->id,
                'liked_by' => $event->likedBy->id,
            ],
            'read_at' => null,
        ]);
        
        // Increment unread count in Redis
        Redis::incr("user:{$event->post->user_id}:unread_notifications");
    }
}

// Efficient notification fetching
public function getNotifications(Request $request)
{
    $notifications = Notification::where('user_id', $request->user()->id)
        ->with('notifiable') // Eager load
        ->latest()
        ->paginate(20);
    
    $unreadCount = Redis::get("user:{$request->user()->id}:unread_notifications") ?? 0;
    
    return response()->json([
        'notifications' => $notifications,
        'unread_count' => $unreadCount,
    ]);
}
```

---

### Scenario 3: Multi-Tenant SaaS Application

**Context:** Building a SaaS platform where each customer has isolated data.

**Q4: How would you implement multi-tenancy?**

```php
// Approach 1: Single database with tenant_id column
// Pros: Simple, cost-effective
// Cons: Risk of data leakage, harder to scale per tenant

// Global scope to auto-filter by tenant
trait BelongsToTenant
{
    protected static function bootBelongsToTenant()
    {
        static::addGlobalScope('tenant', function ($query) {
            if (auth()->check()) {
                $query->where('tenant_id', auth()->user()->tenant_id);
            }
        });
        
        static::creating(function ($model) {
            if (auth()->check()) {
                $model->tenant_id = auth()->user()->tenant_id;
            }
        });
    }
}

// Usage in models
class Post extends Model
{
    use BelongsToTenant;
}

// Middleware to set tenant context
class SetTenantFromSubdomain
{
    public function handle(Request $request, Closure $next)
    {
        $subdomain = explode('.', $request->getHost())[0];
        
        $tenant = Tenant::where('subdomain', $subdomain)->firstOrFail();
        
        // Set in config for global access
        config(['app.current_tenant' => $tenant->id]);
        
        return $next($request);
    }
}

// Approach 2: Separate database per tenant (better isolation)
class TenantDatabaseManager
{
    public function switchToTenant(Tenant $tenant): void
    {
        config([
            'database.connections.tenant' => [
                'driver' => 'mysql',
                'host' => env('DB_HOST'),
                'database' => $tenant->database_name,
                'username' => env('DB_USERNAME'),
                'password' => env('DB_PASSWORD'),
            ]
        ]);
        
        DB::purge('tenant');
        DB::reconnect('tenant');
    }
}

// Usage
$tenantDb = app(TenantDatabaseManager::class);
$tenantDb->switchToTenant($tenant);

$users = DB::connection('tenant')->table('users')->get();
```

---

### Scenario 4: Payment Processing System

**Context:** Integrating multiple payment gateways with proper error handling.

**Q5: How would you handle payment failures and retries?**

```php
// Strategy Pattern for multiple gateways
interface PaymentGateway
{
    public function charge(float $amount, string $token): PaymentResult;
    public function refund(string $transactionId): bool;
}

class StripeGateway implements PaymentGateway
{
    public function charge(float $amount, string $token): PaymentResult
    {
        try {
            $charge = \Stripe\Charge::create([
                'amount' => $amount * 100,
                'currency' => 'usd',
                'source' => $token,
            ]);
            
            return new PaymentResult(
                success: true,
                transactionId: $charge->id,
                message: 'Payment successful'
            );
        } catch (\Stripe\Exception\CardException $e) {
            return new PaymentResult(
                success: false,
                transactionId: null,
                message: $e->getMessage(),
                errorCode: $e->getCode()
            );
        }
    }
}

// Job with retry logic
class ProcessPayment implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;
    
    public $tries = 3;
    public $backoff = [60, 300, 900]; // 1min, 5min, 15min
    
    public function __construct(
        public Order $order,
        public string $paymentToken
    ) {}
    
    public function handle(PaymentGateway $gateway): void
    {
        $result = $gateway->charge($this->order->total, $this->paymentToken);
        
        if ($result->success) {
            $this->order->update([
                'status' => 'paid',
                'transaction_id' => $result->transactionId,
            ]);
            
            event(new OrderPaid($this->order));
        } else {
            // Log the failure
            Log::error('Payment failed', [
                'order_id' => $this->order->id,
                'error' => $result->message,
                'attempt' => $this->attempts(),
            ]);
            
            // Retry if retryable error
            if ($this->isRetryable($result->errorCode)) {
                $this->release($this->backoff[$this->attempts() - 1] ?? 900);
            } else {
                // Permanent failure
                $this->fail(new PaymentException($result->message));
            }
        }
    }
    
    private function isRetryable(string $errorCode): bool
    {
        return in_array($errorCode, [
            'network_error',
            'gateway_timeout',
            'rate_limit_exceeded',
        ]);
    }
    
    public function failed(\Throwable $exception): void
    {
        $this->order->update(['status' => 'payment_failed']);
        
        // Notify customer
        Mail::to($this->order->user)->send(
            new PaymentFailedMail($this->order, $exception->getMessage())
        );
    }
}
```

---

## 🐛 Fast Debugging Techniques

### 1. Systematic Debugging Approach

```
┌─────────────────────────────────────────┐
│  1. REPRODUCE THE BUG                   │
│     - Get exact steps to reproduce      │
│     - Note environment (dev/staging)    │
│     - Check if it's consistent          │
└─────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────┐
│  2. ISOLATE THE PROBLEM                 │
│     - Binary search approach            │
│     - Comment out code sections         │
│     - Check recent changes (git log)    │
└─────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────┐
│  3. GATHER INFORMATION                  │
│     - Check logs (Laravel log, nginx)   │
│     - Database queries (query log)      │
│     - Network requests (browser dev)    │
│     - Error messages (full stack trace) │
└─────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────┐
│  4. FORM HYPOTHESIS                     │
│     - What could cause this?            │
│     - List possible causes              │
│     - Prioritize by likelihood          │
└─────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────┐
│  5. TEST HYPOTHESIS                     │
│     - Add strategic debug points        │
│     - Use debugger (Xdebug)             │
│     - Write failing test                │
└─────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────┐
│  6. FIX AND VERIFY                      │
│     - Implement fix                     │
│     - Test thoroughly                   │
│     - Add regression test               │
└─────────────────────────────────────────┘
```

---

### 2. Laravel-Specific Debugging Tools

```php
// 1. Query Debugging - Find slow queries
DB::listen(function ($query) {
    if ($query->time > 100) { // Queries taking > 100ms
        Log::warning('Slow query detected', [
            'sql' => $query->sql,
            'bindings' => $query->bindings,
            'time' => $query->time,
        ]);
    }
});

// 2. Enable query log temporarily
DB::enableQueryLog();
// ... your code ...
dd(DB::getQueryLog());

// 3. Debug specific Eloquent query
$query = User::where('active', true)->with('posts');
dd($query->toSql(), $query->getBindings());

// 4. Ray - Modern debugging tool (better than dd)
ray($user)->blue(); // Color-coded output
ray()->table($users); // Table format
ray()->measure(function () {
    // Code to measure
}); // Execution time

// 5. Laravel Telescope - Request debugging
// Install: composer require laravel/telescope
// Access: /telescope
// Shows: Requests, Queries, Jobs, Events, Logs, etc.

// 6. Clockwork - Browser extension
// Shows detailed request info in browser dev tools

// 7. Custom debug helper
if (!function_exists('debug_log')) {
    function debug_log($message, $data = [])
    {
        Log::channel('debug')->info($message, array_merge($data, [
            'url' => request()->fullUrl(),
            'user_id' => auth()->id(),
            'timestamp' => now()->toDateTimeString(),
            'memory' => memory_get_usage(true) / 1024 / 1024 . ' MB',
        ]));
    }
}

// Usage
debug_log('User registration attempt', ['email' => $email]);
```

---

### 3. Performance Debugging

```php
// Identify N+1 Query Problems
// Install: composer require barryvdh/laravel-debugbar

// Example N+1 problem
$posts = Post::all();
foreach ($posts as $post) {
    echo $post->user->name; // N+1 query!
}

// Fix with eager loading
$posts = Post::with('user')->get();

// Profile specific code block
$start = microtime(true);
// ... code to profile ...
$end = microtime(true);
Log::info('Execution time: ' . ($end - $start) . ' seconds');

// Memory profiling
$memoryBefore = memory_get_usage();
// ... code ...
$memoryAfter = memory_get_usage();
Log::info('Memory used: ' . (($memoryAfter - $memoryBefore) / 1024 / 1024) . ' MB');

// Laravel Debugbar - Shows all queries, memory, time
// Access via toolbar at bottom of page in dev mode

// Find bottlenecks with Blackfire.io or Xdebug profiler
// Generate cachegrind file and analyze with tools like KCachegrind
```

---

### 4. JavaScript Debugging Techniques

```javascript
// 1. Strategic console logging
console.log('🔵 User data:', user);
console.warn('⚠️ Deprecated function called');
console.error('❌ API request failed:', error);
console.table(users); // Table format for arrays
console.time('API Call');
// ... API call ...
console.timeEnd('API Call'); // Shows duration

// 2. Debugger statement
function processData(data) {
    debugger; // Execution pauses here in dev tools
    return data.map(item => item.value);
}

// 3. Conditional breakpoints in browser
// Right-click breakpoint → Add conditional breakpoint
// Condition: user.id === 123

// 4. Network debugging
fetch('/api/users')
    .then(response => {
        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);
        return response.json();
    })
    .then(data => console.log('Data:', data))
    .catch(error => console.error('Error:', error));

// 5. Performance monitoring
performance.mark('start-fetch');
await fetchData();
performance.mark('end-fetch');
performance.measure('fetch-duration', 'start-fetch', 'end-fetch');
console.log(performance.getEntriesByName('fetch-duration')[0].duration);

// 6. Error boundary (React)
class ErrorBoundary extends React.Component {
    componentDidCatch(error, errorInfo) {
        console.error('Error caught:', error, errorInfo);
        // Log to error tracking service
        logErrorToService(error, errorInfo);
    }
    
    render() {
        return this.props.children;
    }
}
```

---

### 5. Database Debugging

```sql
-- 1. EXPLAIN query execution plan
EXPLAIN SELECT * FROM users WHERE email = 'test@example.com';

-- 2. Show slow queries
SHOW FULL PROCESSLIST;

-- 3. Check table indexes
SHOW INDEX FROM users;

-- 4. Analyze table statistics
ANALYZE TABLE users;

-- 5. Check for locks
SHOW ENGINE INNODB STATUS;

-- 6. Profile query
SET profiling = 1;
SELECT * FROM users WHERE active = 1;
SHOW PROFILES;
SHOW PROFILE FOR QUERY 1;

-- 7. Find missing indexes
SELECT 
    table_name,
    index_name,
    column_name
FROM information_schema.statistics
WHERE table_schema = 'your_database'
ORDER BY table_name, index_name;
```

---

### 6. Production Debugging Checklist

```bash
# 1. Check application logs
tail -f storage/logs/laravel.log

# 2. Check web server logs
tail -f /var/log/nginx/error.log
tail -f /var/log/nginx/access.log

# 3. Check PHP-FPM logs
tail -f /var/log/php8.1-fpm.log

# 4. Check system resources
top
htop
free -h
df -h

# 5. Check database connections
mysql -e "SHOW PROCESSLIST;"

# 6. Check queue workers
php artisan queue:work --once --verbose

# 7. Check scheduled tasks
php artisan schedule:list

# 8. Clear caches if needed
php artisan cache:clear
php artisan config:clear
php artisan route:clear
php artisan view:clear

# 9. Check for failed jobs
php artisan queue:failed

# 10. Monitor in real-time
php artisan tail
```

---

### 7. Common Bug Patterns & Quick Fixes

```php
// Pattern 1: Null pointer exceptions
// Bad
$user->profile->bio; // Error if profile is null

// Good
$user->profile?->bio; // PHP 8 nullsafe operator
optional($user->profile)->bio; // Laravel helper

// Pattern 2: Mass assignment vulnerability
// Bad
User::create($request->all()); // Dangerous!

// Good
User::create($request->only(['name', 'email']));

// Pattern 3: Memory leaks in loops
// Bad
foreach (User::all() as $user) { // Loads all in memory
    processUser($user);
}

// Good
User::chunk(100, function ($users) {
    foreach ($users as $user) {
        processUser($user);
    }
});

// Pattern 4: Timezone issues
// Bad
$date = Carbon::now(); // Uses server timezone

// Good
$date = Carbon::now('UTC');
$userDate = $date->setTimezone($user->timezone);

// Pattern 5: Race conditions
// Bad
if (!Cache::has('key')) {
    Cache::put('key', $value);
}

// Good
Cache::add('key', $value); // Atomic operation

// Pattern 6: SQL injection (even with Eloquent)
// Bad
User::whereRaw("name = '$name'")->get(); // Vulnerable!

// Good
User::whereRaw("name = ?", [$name])->get();
User::where('name', $name)->get(); // Best
```

---

## 🚨 Production Issues & Solutions

### Issue 1: Application Suddenly Slow

**Debugging Steps:**
```bash
# 1. Check server resources
top
# Look for: High CPU, memory usage

# 2. Check database
mysql -e "SHOW FULL PROCESSLIST;"
# Look for: Long-running queries

# 3. Check Laravel logs
tail -100 storage/logs/laravel.log
# Look for: Errors, slow query warnings

# 4. Check queue
php artisan queue:failed
# Look for: Failed jobs backing up

# 5. Profile with Telescope
# Visit /telescope/requests
# Sort by duration
```

**Common Causes & Fixes:**
```php
// Cause 1: N+1 queries
// Fix: Add eager loading
Post::with('user', 'comments')->get();

// Cause 2: Missing cache
// Fix: Add caching layer
Cache::remember('posts', 3600, fn() => Post::all());

// Cause 3: Unoptimized queries
// Fix: Add indexes, optimize joins

// Cause 4: Memory leak
// Fix: Use chunk() or cursor()
```

---

### Issue 2: 500 Internal Server Error

**Debugging Steps:**
```php
// 1. Enable detailed errors (dev only!)
// .env
APP_DEBUG=true

// 2. Check Laravel log
tail -f storage/logs/laravel.log

// 3. Check PHP error log
tail -f /var/log/php-fpm/error.log

// 4. Check permissions
ls -la storage/
# Should be writable by web server

// 5. Check .env configuration
php artisan config:cache
php artisan route:cache
```

---

### Issue 3: Database Connection Errors

```php
// Debugging
// 1. Test connection
php artisan tinker
>>> DB::connection()->getPdo();

// 2. Check connection pool
DB::select("SHOW STATUS LIKE 'Threads_connected'");

// 3. Increase connection limit
// config/database.php
'mysql' => [
    'options' => [
        PDO::ATTR_PERSISTENT => true,
    ],
],

// 4. Use connection pooling (PgBouncer for PostgreSQL)
```

---

## 🎯 Interview Tips for Project Questions

### How to Answer Project Scenario Questions

1. **Clarify Requirements**
   - "How many users are we expecting?"
   - "What's the acceptable response time?"
   - "Do we need real-time updates?"

2. **Start with High-Level Architecture**
   - Draw a diagram
   - Identify components
   - Explain data flow

3. **Discuss Trade-offs**
   - "We could use X for simplicity, but Y scales better"
   - "This approach is faster but uses more memory"

4. **Mention Monitoring**
   - "I'd add logging here"
   - "We should monitor this metric"
   - "Alert if this threshold is exceeded"

5. **Consider Edge Cases**
   - "What if the payment gateway is down?"
   - "How do we handle concurrent requests?"
   - "What about data consistency?"

---

## 📚 Debugging Resources

### Tools
- **Laravel Telescope** - Request debugging
- **Laravel Debugbar** - Query profiling
- **Ray** - Modern debugging
- **Xdebug** - PHP debugger
- **Clockwork** - Browser extension

### Services
- **Sentry** - Error tracking
- **New Relic** - APM
- **Datadog** - Monitoring
- **Blackfire.io** - Profiling

### Commands
```bash
# Laravel debugging commands
php artisan route:list
php artisan event:list
php artisan queue:work --verbose
php artisan schedule:list
php artisan telescope:prune
```

---

**Remember:** 
- **Reproduce first** - Can't fix what you can't reproduce
- **Isolate the problem** - Binary search approach
- **Read error messages** - They usually tell you exactly what's wrong
- **Check recent changes** - `git log` is your friend
- **Use the right tools** - Don't debug with `var_dump` in 2024!

Good luck debugging! 🐛➡️✅
