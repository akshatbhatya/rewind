# 🔥 Laravel Best Practices & Interview Guide (3 Years Experience)

**Focus:** Modern Laravel (8.x - 11.x), Architecture, Design Patterns, Performance, and Production-Ready Code

This comprehensive guide contains **35+ Laravel interview questions** with real-world implementations and industry best practices.

---

## Table of Contents
1. [Routing & Controllers](#1-routing--controllers)
2. [Eloquent ORM & Database](#2-eloquent-orm--database)
3. [Middleware & Request Lifecycle](#3-middleware--request-lifecycle)
4. [Service Container & Dependency Injection](#4-service-container--dependency-injection)
5. [Queues & Jobs](#5-queues--jobs)
6. [Events & Listeners](#6-events--listeners)
7. [Authentication & Authorization](#7-authentication--authorization)
8. [API Development](#8-api-development)
9. [Testing](#9-testing)
10. [Performance & Caching](#10-performance--caching)

---

## 1. Routing & Controllers

### Q1: RESTful Resource Controllers with Best Practices

```php
// routes/api.php
use App\Http\Controllers\Api\UserController;

Route::middleware('auth:sanctum')->group(function () {
    // Resource routes (RESTful)
    Route::apiResource('users', UserController::class);
    
    // Custom actions
    Route::post('users/{user}/restore', [UserController::class, 'restore'])
        ->name('users.restore');
    
    // Nested resources
    Route::apiResource('users.posts', UserPostController::class);
});

// app/Http/Controllers/Api/UserController.php
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreUserRequest;
use App\Http\Requests\UpdateUserRequest;
use App\Http\Resources\UserResource;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;

class UserController extends Controller
{
    /**
     * Display a listing of users.
     */
    public function index(): AnonymousResourceCollection
    {
        $users = User::query()
            ->when(request('search'), function ($query, $search) {
                $query->where('name', 'like', "%{$search}%")
                    ->orWhere('email', 'like', "%{$search}%");
            })
            ->when(request('status'), function ($query, $status) {
                $query->where('status', $status);
            })
            ->latest()
            ->paginate(request('per_page', 15));
        
        return UserResource::collection($users);
    }
    
    /**
     * Store a newly created user.
     */
    public function store(StoreUserRequest $request): JsonResponse
    {
        $user = User::create($request->validated());
        
        return response()->json([
            'message' => 'User created successfully',
            'data' => new UserResource($user)
        ], 201);
    }
    
    /**
     * Display the specified user.
     */
    public function show(User $user): UserResource
    {
        // Eager load relationships
        $user->load(['posts', 'profile']);
        
        return new UserResource($user);
    }
    
    /**
     * Update the specified user.
     */
    public function update(UpdateUserRequest $request, User $user): UserResource
    {
        $user->update($request->validated());
        
        return new UserResource($user->fresh());
    }
    
    /**
     * Remove the specified user.
     */
    public function destroy(User $user): JsonResponse
    {
        $user->delete();
        
        return response()->json([
            'message' => 'User deleted successfully'
        ], 204);
    }
}

// Form Request Validation
// app/Http/Requests/StoreUserRequest.php
namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rules\Password;

class StoreUserRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true; // Or check permissions
    }
    
    public function rules(): array
    {
        return [
            'name' => ['required', 'string', 'max:255'],
            'email' => ['required', 'email', 'unique:users,email'],
            'password' => ['required', 'confirmed', Password::defaults()],
            'role' => ['sometimes', 'in:admin,user,moderator'],
        ];
    }
    
    public function messages(): array
    {
        return [
            'email.unique' => 'This email is already registered.',
        ];
    }
}
```

**Best Practices:**
✅ Use Route Model Binding  
✅ Separate Form Requests for validation  
✅ Use API Resources for response formatting  
✅ Return appropriate HTTP status codes  
✅ Use query scopes in models, not controllers

---

### Q2: Advanced Routing Techniques

```php
// Route Groups with Multiple Middleware
Route::middleware(['auth', 'verified', 'throttle:60,1'])
    ->prefix('admin')
    ->name('admin.')
    ->group(function () {
        Route::get('dashboard', [AdminController::class, 'dashboard']);
        Route::resource('users', AdminUserController::class);
    });

// Route Model Binding with Custom Logic
Route::get('posts/{post:slug}', [PostController::class, 'show']);

// Custom Resolution
use Illuminate\Support\Facades\Route;

Route::bind('user', function (string $value) {
    return User::where('uuid', $value)->firstOrFail();
});

// Rate Limiting (Custom)
use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Support\Facades\RateLimiter;

RateLimiter::for('api', function (Request $request) {
    return Limit::perMinute(60)->by($request->user()?->id ?: $request->ip());
});

// Dynamic rate limiting based on user type
RateLimiter::for('uploads', function (Request $request) {
    return $request->user()->isPremium()
        ? Limit::none()
        : Limit::perMinute(10);
});

// Fallback Routes
Route::fallback(function () {
    return response()->json([
        'message' => 'Route not found'
    ], 404);
});

// Route Caching (Production)
// php artisan route:cache
// php artisan route:clear
```

---

## 2. Eloquent ORM & Database

### Q3: Eloquent Relationships - All Types with Examples

```php
// app/Models/User.php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\HasOne;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasManyThrough;

class User extends Model
{
    // One-to-One
    public function profile(): HasOne
    {
        return $this->hasOne(Profile::class);
    }
    
    // One-to-Many
    public function posts(): HasMany
    {
        return $this->hasMany(Post::class);
    }
    
    // Many-to-Many
    public function roles(): BelongsToMany
    {
        return $this->belongsToMany(Role::class)
            ->withPivot(['assigned_at', 'assigned_by'])
            ->withTimestamps();
    }
    
    // Has Many Through
    public function comments(): HasManyThrough
    {
        return $this->hasManyThrough(Comment::class, Post::class);
    }
    
    // Polymorphic Relations
    public function images(): MorphMany
    {
        return $this->morphMany(Image::class, 'imageable');
    }
    
    // Many-to-Many Polymorphic
    public function tags(): MorphToMany
    {
        return $this->morphToMany(Tag::class, 'taggable');
    }
}

// Query Optimization - Eager Loading
// Bad (N+1 Problem)
$users = User::all();
foreach ($users as $user) {
    echo $user->profile->bio; // N queries!
}

// Good
$users = User::with('profile')->get();
foreach ($users as $user) {
    echo $user->profile->bio; // 2 queries only
}

// Lazy Eager Loading
$users = User::all();
if ($someCondition) {
    $users->load('posts.comments');
}

// Eager Load Specific Columns
$users = User::with('profile:id,user_id,bio')->get();

// Conditional Eager Loading
$users = User::with([
    'posts' => function ($query) {
        $query->where('published', true)
            ->orderBy('created_at', 'desc')
            ->limit(5);
    }
])->get();

// Count Relations Without Loading
$users = User::withCount('posts')->get();
foreach ($users as $user) {
    echo $user->posts_count; // No additional queries
}
```

---

### Q4: Query Scopes and Advanced Eloquent

```php
// app/Models/Post.php
namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

class Post extends Model
{
    use SoftDeletes;
    
    protected $fillable = ['title', 'content', 'status', 'published_at'];
    
    protected $casts = [
        'published_at' => 'datetime',
        'metadata' => 'array',
    ];
    
    // Local Scope
    public function scopePublished(Builder $query): Builder
    {
        return $query->where('status', 'published')
            ->whereNotNull('published_at')
            ->where('published_at', '<=', now());
    }
    
    public function scopePopular(Builder $query): Builder
    {
        return $query->where('views', '>', 1000)
            ->orderBy('views', 'desc');
    }
    
    // Dynamic Scope
    public function scopeOfType(Builder $query, string $type): Builder
    {
        return $query->where('type', $type);
    }
    
    // Global Scope (applied to all queries)
    protected static function booted(): void
    {
        static::addGlobalScope('active', function (Builder $builder) {
            $builder->where('active', true);
        });
    }
}

// Usage
$posts = Post::published()->popular()->get();
$articles = Post::ofType('article')->latest()->paginate(10);

// Accessor (get attribute)
public function getTitleAttribute($value): string
{
    return ucfirst($value);
}

// Mutator (set attribute)
public function setTitleAttribute($value): void
{
    $this->attributes['title'] = strtolower($value);
    $this->attributes['slug'] = Str::slug($value);
}

// Custom Cast
use Illuminate\Contracts\Database\Eloquent\CastsAttributes;

class Money implements CastsAttributes
{
    public function get($model, string $key, $value, array $attributes)
    {
        return $value / 100; // Store as cents
    }
    
    public function set($model, string $key, $value, array $attributes)
    {
        return $value * 100;
    }
}

// In Model
protected $casts = [
    'price' => Money::class,
];

// Advanced Queries
// Subquery
$users = User::select('users.*')
    ->selectSub(function ($query) {
        $query->selectRaw('count(*)')
            ->from('posts')
            ->whereColumn('posts.user_id', 'users.id');
    }, 'posts_count')
    ->having('posts_count', '>', 5)
    ->get();

// Chunk Processing (Memory Efficient)
Post::chunk(200, function ($posts) {
    foreach ($posts as $post) {
        // Process each post
    }
});

// Lazy Collections (Even More Memory Efficient)
Post::cursor()->each(function ($post) {
    // Process one at a time
});

// Upsert (Insert or Update)
User::upsert(
    [
        ['email' => 'john@example.com', 'name' => 'John'],
        ['email' => 'jane@example.com', 'name' => 'Jane'],
    ],
    ['email'], // Unique columns
    ['name']   // Columns to update
);
```

---

### Q5: Database Transactions and Locking

```php
use Illuminate\Support\Facades\DB;

// Basic Transaction
DB::transaction(function () {
    $user = User::create([...]);
    $user->profile()->create([...]);
    $user->assignRole('user');
});

// Manual Transaction Control
DB::beginTransaction();

try {
    $order = Order::create($orderData);
    
    foreach ($items as $item) {
        $order->items()->create($item);
        
        // Decrement stock
        Product::where('id', $item['product_id'])
            ->decrement('stock', $item['quantity']);
    }
    
    DB::commit();
} catch (\Exception $e) {
    DB::rollBack();
    throw $e;
}

// Pessimistic Locking
DB::transaction(function () {
    $user = User::where('id', 1)
        ->lockForUpdate() // SELECT ... FOR UPDATE
        ->first();
    
    $user->balance -= 100;
    $user->save();
});

// Shared Lock (Read Lock)
$users = User::where('active', true)
    ->sharedLock()
    ->get();

// Optimistic Locking (using version column)
// Migration
Schema::table('posts', function (Blueprint $table) {
    $table->integer('version')->default(0);
});

// Model
public function save(array $options = [])
{
    $this->version++;
    
    $result = parent::save($options);
    
    if ($this->wasChanged('version') && $this->version > 1) {
        throw new \Exception('Concurrent modification detected');
    }
    
    return $result;
}
```

---

## 3. Middleware & Request Lifecycle

### Q6: Custom Middleware Implementation

```php
// app/Http/Middleware/CheckUserRole.php
namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class CheckUserRole
{
    /**
     * Handle an incoming request.
     */
    public function handle(Request $request, Closure $next, string ...$roles)
    {
        if (!$request->user()) {
            return response()->json(['message' => 'Unauthenticated'], 401);
        }
        
        if (!$request->user()->hasAnyRole($roles)) {
            return response()->json([
                'message' => 'Insufficient permissions'
            ], 403);
        }
        
        return $next($request);
    }
}

// Register in app/Http/Kernel.php
protected $routeMiddleware = [
    'role' => \App\Http\Middleware\CheckUserRole::class,
];

// Usage
Route::middleware('role:admin,moderator')->group(function () {
    Route::get('admin/dashboard', [AdminController::class, 'dashboard']);
});

// Terminable Middleware (runs after response sent)
class LogAfterRequest
{
    public function handle(Request $request, Closure $next)
    {
        return $next($request);
    }
    
    public function terminate(Request $request, $response)
    {
        // Log request after response sent
        Log::info('Request completed', [
            'url' => $request->fullUrl(),
            'method' => $request->method(),
            'status' => $response->status(),
        ]);
    }
}

// API Rate Limiting Middleware
class ApiRateLimiter
{
    public function handle(Request $request, Closure $next)
    {
        $key = 'api_rate_limit:' . $request->ip();
        $maxAttempts = 60;
        $decayMinutes = 1;
        
        if (RateLimiter::tooManyAttempts($key, $maxAttempts)) {
            return response()->json([
                'message' => 'Too many requests'
            ], 429);
        }
        
        RateLimiter::hit($key, $decayMinutes * 60);
        
        $response = $next($request);
        
        // Add rate limit headers
        $response->headers->set('X-RateLimit-Limit', $maxAttempts);
        $response->headers->set('X-RateLimit-Remaining', 
            $maxAttempts - RateLimiter::attempts($key));
        
        return $response;
    }
}
```

---

## 4. Service Container & Dependency Injection

### Q7: Service Container and Binding

```php
// app/Providers/AppServiceProvider.php
use App\Contracts\PaymentGatewayInterface;
use App\Services\StripePaymentGateway;

public function register(): void
{
    // Simple binding
    $this->app->bind(PaymentGatewayInterface::class, StripePaymentGateway::class);
    
    // Singleton binding (one instance shared)
    $this->app->singleton(CacheManager::class, function ($app) {
        return new CacheManager($app['config']['cache']);
    });
    
    // Contextual binding
    $this->app->when(AdminController::class)
        ->needs(PaymentGatewayInterface::class)
        ->give(StripePaymentGateway::class);
    
    $this->app->when(UserController::class)
        ->needs(PaymentGatewayInterface::class)
        ->give(PayPalPaymentGateway::class);
    
    // Instance binding
    $this->app->instance('config.cache', $cacheConfig);
}

// Interface
// app/Contracts/PaymentGatewayInterface.php
namespace App\Contracts;

interface PaymentGatewayInterface
{
    public function charge(float $amount, string $token): bool;
    public function refund(string $transactionId): bool;
}

// Implementation
// app/Services/StripePaymentGateway.php
namespace App\Services;

class StripePaymentGateway implements PaymentGatewayInterface
{
    public function __construct(
        private string $apiKey
    ) {}
    
    public function charge(float $amount, string $token): bool
    {
        // Stripe implementation
        return true;
    }
    
    public function refund(string $transactionId): bool
    {
        return true;
    }
}

// Controller with Dependency Injection
namespace App\Http\Controllers;

use App\Contracts\PaymentGatewayInterface;

class PaymentController extends Controller
{
    public function __construct(
        private PaymentGatewayInterface $paymentGateway
    ) {}
    
    public function charge(Request $request)
    {
        $success = $this->paymentGateway->charge(
            $request->amount,
            $request->token
        );
        
        return response()->json(['success' => $success]);
    }
}

// Service Provider Pattern
// app/Services/UserService.php
namespace App\Services;

use App\Repositories\UserRepository;
use Illuminate\Support\Facades\Hash;

class UserService
{
    public function __construct(
        private UserRepository $userRepository
    ) {}
    
    public function createUser(array $data): User
    {
        $data['password'] = Hash::make($data['password']);
        
        $user = $this->userRepository->create($data);
        
        event(new UserCreated($user));
        
        return $user;
    }
    
    public function updateUser(User $user, array $data): User
    {
        if (isset($data['password'])) {
            $data['password'] = Hash::make($data['password']);
        }
        
        return $this->userRepository->update($user, $data);
    }
}

// Repository Pattern
// app/Repositories/UserRepository.php
namespace App\Repositories;

use App\Models\User;

class UserRepository
{
    public function create(array $data): User
    {
        return User::create($data);
    }
    
    public function update(User $user, array $data): User
    {
        $user->update($data);
        return $user->fresh();
    }
    
    public function findByEmail(string $email): ?User
    {
        return User::where('email', $email)->first();
    }
}
```

---

## 5. Queues & Jobs

### Q8: Queue Jobs with Error Handling

```php
// app/Jobs/ProcessOrderJob.php
namespace App\Jobs;

use App\Models\Order;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;

class ProcessOrderJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;
    
    public $tries = 3;
    public $timeout = 120;
    public $maxExceptions = 3;
    
    /**
     * Exponential backoff
     */
    public function backoff(): array
    {
        return [1, 5, 10]; // Retry after 1s, 5s, 10s
    }
    
    public function __construct(
        public Order $order
    ) {}
    
    public function handle(): void
    {
        // Process order
        $this->order->update(['status' => 'processing']);
        
        // Send confirmation email
        Mail::to($this->order->user)->send(new OrderConfirmation($this->order));
        
        // Update inventory
        foreach ($this->order->items as $item) {
            $item->product->decrement('stock', $item->quantity);
        }
        
        $this->order->update(['status' => 'completed']);
    }
    
    /**
     * Handle a job failure.
     */
    public function failed(\Throwable $exception): void
    {
        // Send notification to admin
        Notification::send(
            User::admins()->get(),
            new OrderProcessingFailed($this->order, $exception)
        );
        
        // Update order status
        $this->order->update(['status' => 'failed']);
    }
}

// Dispatch the job
ProcessOrderJob::dispatch($order);

// Dispatch with delay
ProcessOrderJob::dispatch($order)->delay(now()->addMinutes(5));

// Dispatch to specific queue
ProcessOrderJob::dispatch($order)->onQueue('emails');

// Chain jobs
SendWelcomeEmail::withChain([
    new VerifyEmail($user),
    new SendCoupon($user),
])->dispatch($user);

// Batch jobs (Laravel 8+)
use Illuminate\Support\Facades\Bus;

$batch = Bus::batch([
    new ProcessOrder($order1),
    new ProcessOrder($order2),
    new ProcessOrder($order3),
])->then(function (Batch $batch) {
    // All jobs completed successfully
})->catch(function (Batch $batch, \Throwable $e) {
    // First batch job failure
})->finally(function (Batch $batch) {
    // The batch has finished executing
})->dispatch();

// Check batch status
$batch = Bus::findBatch($batchId);
$batch->progress(); // Percentage completed

// Job Middleware
// app/Jobs/Middleware/RateLimited.php
namespace App\Jobs\Middleware;

use Illuminate\Support\Facades\Redis;

class RateLimited
{
    public function handle($job, $next)
    {
        Redis::throttle('key')
            ->block(0)
            ->allow(10)
            ->every(60)
            ->then(function () use ($next, $job) {
                $next($job);
            }, function () use ($job) {
                $job->release(10);
            });
    }
}

// Use middleware in job
public function middleware(): array
{
    return [new RateLimited];
}
```

---

## 6. Events & Listeners

### Q9: Event-Driven Architecture

```php
// app/Events/OrderPlaced.php
namespace App\Events;

use App\Models\Order;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class OrderPlaced
{
    use Dispatchable, SerializesModels;
    
    public function __construct(
        public Order $order
    ) {}
}

// app/Listeners/SendOrderConfirmation.php
namespace App\Listeners;

use App\Events\OrderPlaced;
use App\Mail\OrderConfirmationMail;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Support\Facades\Mail;

class SendOrderConfirmation implements ShouldQueue
{
    public $queue = 'emails';
    
    public function handle(OrderPlaced $event): void
    {
        Mail::to($event->order->user)
            ->send(new OrderConfirmationMail($event->order));
    }
    
    public function failed(OrderPlaced $event, \Throwable $exception): void
    {
        // Handle failure
    }
}

// app/Listeners/UpdateInventory.php
class UpdateInventory
{
    public function handle(OrderPlaced $event): void
    {
        foreach ($event->order->items as $item) {
            $item->product->decrement('stock', $item->quantity);
        }
    }
}

// Register in EventServiceProvider
protected $listen = [
    OrderPlaced::class => [
        SendOrderConfirmation::class,
        UpdateInventory::class,
        NotifyAdmin::class,
    ],
];

// Or use auto-discovery (Laravel 8+)
// Events will be auto-discovered in app/Events and app/Listeners

// Dispatch event
OrderPlaced::dispatch($order);

// Event Subscribers (group listeners)
// app/Listeners/UserEventSubscriber.php
namespace App\Listeners;

class UserEventSubscriber
{
    public function handleUserLogin($event) {}
    
    public function handleUserLogout($event) {}
    
    public function subscribe($events)
    {
        $events->listen(
            'Illuminate\Auth\Events\Login',
            [UserEventSubscriber::class, 'handleUserLogin']
        );
        
        $events->listen(
            'Illuminate\Auth\Events\Logout',
            [UserEventSubscriber::class, 'handleUserLogout']
        );
    }
}

// Register subscriber
protected $subscribe = [
    UserEventSubscriber::class,
];

// Observer Pattern for Models
// app/Observers/UserObserver.php
namespace App\Observers;

use App\Models\User;

class UserObserver
{
    public function creating(User $user): void
    {
        $user->uuid = Str::uuid();
    }
    
    public function created(User $user): void
    {
        // Send welcome email
        Mail::to($user)->send(new WelcomeMail($user));
    }
    
    public function updating(User $user): void
    {
        if ($user->isDirty('email')) {
            $user->email_verified_at = null;
        }
    }
    
    public function deleted(User $user): void
    {
        // Cleanup related data
        $user->posts()->delete();
    }
}

// Register observer in AppServiceProvider
public function boot(): void
{
    User::observe(UserObserver::class);
}
```

---

## 7. Authentication & Authorization

### Q10: Laravel Sanctum API Authentication

```php
// config/sanctum.php - Configure
'expiration' => 60 * 24, // 24 hours

// app/Http/Controllers/Auth/LoginController.php
namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use App\Models\User;

class LoginController extends Controller
{
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
            'device_name' => 'required',
        ]);
        
        $user = User::where('email', $request->email)->first();
        
        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json([
                'message' => 'Invalid credentials'
            ], 401);
        }
        
        // Create token
        $token = $user->createToken(
            $request->device_name,
            ['read', 'write'] // Abilities/scopes
        )->plainTextToken;
        
        return response()->json([
            'token' => $token,
            'user' => $user
        ]);
    }
    
    public function logout(Request $request)
    {
        // Revoke current token
        $request->user()->currentAccessToken()->delete();
        
        return response()->json([
            'message' => 'Logged out successfully'
        ]);
    }
    
    public function logoutAll(Request $request)
    {
        // Revoke all tokens
        $request->user()->tokens()->delete();
        
        return response()->json([
            'message' => 'Logged out from all devices'
        ]);
    }
}

// Protect routes
Route::middleware('auth:sanctum')->group(function () {
    Route::get('/user', function (Request $request) {
        return $request->user();
    });
});

// Check token abilities
Route::middleware('auth:sanctum', 'abilities:read,write')->group(function () {
    Route::post('/posts', [PostController::class, 'store']);
});
```

---

### Q11: Gates and Policies for Authorization

```php
// app/Policies/PostPolicy.php
namespace App\Policies;

use App\Models\User;
use App\Models\Post;

class PostPolicy
{
    /**
     * Determine if user can view any posts
     */
    public function viewAny(User $user): bool
    {
        return true;
    }
    
    /**
     * Determine if user can view the post
     */
    public function view(?User $user, Post $post): bool
    {
        return $post->published || $user?->id === $post->user_id;
    }
    
    /**
     * Determine if user can create posts
     */
    public function create(User $user): bool
    {
        return $user->email_verified_at !== null;
    }
    
    /**
     * Determine if user can update the post
     */
    public function update(User $user, Post $post): bool
    {
        return $user->id === $post->user_id;
    }
    
    /**
     * Determine if user can delete the post
     */
    public function delete(User $user, Post $post): bool
    {
        return $user->id === $post->user_id || $user->isAdmin();
    }
    
    /**
     * Run before all other authorization checks
     */
    public function before(User $user, string $ability): ?bool
    {
        if ($user->isSuperAdmin()) {
            return true;
        }
        
        return null; // Continue to other checks
    }
}

// Register in AuthServiceProvider
protected $policies = [
    Post::class => PostPolicy::class,
];

// Usage in Controller
public function update(Request $request, Post $post)
{
    $this->authorize('update', $post);
    
    // Or
    if ($request->user()->cannot('update', $post)) {
        abort(403);
    }
    
    $post->update($request->validated());
    
    return new PostResource($post);
}

// Gates (for non-model authorization)
// Define in AuthServiceProvider
Gate::define('manage-settings', function (User $user) {
    return $user->isAdmin();
});

Gate::define('access-admin-panel', function (User $user) {
    return $user->hasRole(['admin', 'moderator']);
});

// Usage
if (Gate::allows('manage-settings')) {
    // User can manage settings
}

// In Blade
@can('update', $post)
    <a href="{{ route('posts.edit', $post) }}">Edit</a>
@endcan

// Middleware
Route::middleware('can:manage-settings')->group(function () {
    // Admin routes
});
```

---

## 8. API Development

### Q12: API Resources and Transformers

```php
// app/Http/Resources/UserResource.php
namespace App\Http\Resources;

use Illuminate\Http\Resources\Json\JsonResource;

class UserResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     */
    public function toArray($request): array
    {
        return [
            'id' => $this->id,
            'name' => $this->name,
            'email' => $this->email,
            'created_at' => $this->created_at->toISOString(),
            
            // Conditional attributes
            'email_verified' => $this->when(
                $request->user()?->isAdmin(),
                $this->email_verified_at !== null
            ),
            
            // Relationships
            'posts' => PostResource::collection($this->whenLoaded('posts')),
            'posts_count' => $this->when(
                isset($this->posts_count),
                $this->posts_count
            ),
            
            // Pivot data
            'role' => $this->whenPivotLoaded('role_user', function () {
                return $this->pivot->role;
            }),
        ];
    }
    
    /**
     * Add metadata
     */
    public function with($request): array
    {
        return [
            'version' => '1.0',
        ];
    }
}

// Resource Collection
// app/Http/Resources/UserCollection.php
namespace App\Http\Resources;

use Illuminate\Http\Resources\Json\ResourceCollection;

class UserCollection extends ResourceCollection
{
    public function toArray($request): array
    {
        return [
            'data' => $this->collection,
            'meta' => [
                'total' => $this->total(),
                'current_page' => $this->currentPage(),
                'per_page' => $this->perPage(),
            ],
        ];
    }
}

// Usage
return new UserResource($user);
return UserResource::collection($users);
return new UserCollection(User::paginate());

// Conditional Relationships
public function toArray($request): array
{
    return [
        'id' => $this->id,
        'posts' => PostResource::collection(
            $this->whenLoaded('posts')
        ),
        'latest_post' => new PostResource(
            $this->whenLoaded('latestPost')
        ),
    ];
}
```

---

### Q13: API Versioning

```php
// routes/api/v1.php
Route::prefix('v1')->group(function () {
    Route::apiResource('users', V1\UserController::class);
});

// routes/api/v2.php
Route::prefix('v2')->group(function () {
    Route::apiResource('users', V2\UserController::class);
});

// RouteServiceProvider
public function boot(): void
{
    Route::middleware('api')
        ->prefix('api/v1')
        ->group(base_path('routes/api/v1.php'));
    
    Route::middleware('api')
        ->prefix('api/v2')
        ->group(base_path('routes/api/v2.php'));
}

// Header-based versioning
Route::middleware('api.version:v1')->group(function () {
    // v1 routes
});

// Middleware
class ApiVersion
{
    public function handle($request, Closure $next, $version)
    {
        $requestVersion = $request->header('API-Version', 'v1');
        
        if ($requestVersion !== $version) {
            return response()->json([
                'error' => 'Invalid API version'
            ], 400);
        }
        
        return $next($request);
    }
}
```

---

## 9. Testing

### Q14: Feature Tests with Database

```php
// tests/Feature/UserApiTest.php
namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class UserApiTest extends TestCase
{
    use RefreshDatabase;
    
    public function test_can_list_users()
    {
        $users = User::factory()->count(3)->create();
        
        $response = $this->getJson('/api/users');
        
        $response->assertStatus(200)
            ->assertJsonCount(3, 'data')
            ->assertJsonStructure([
                'data' => [
                    '*' => ['id', 'name', 'email', 'created_at']
                ]
            ]);
    }
    
    public function test_can_create_user()
    {
        $userData = [
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'password' => 'password123',
            'password_confirmation' => 'password123',
        ];
        
        $response = $this->postJson('/api/users', $userData);
        
        $response->assertStatus(201)
            ->assertJsonPath('data.name', 'John Doe');
        
        $this->assertDatabaseHas('users', [
            'email' => 'john@example.com'
        ]);
    }
    
    public function test_cannot_create_user_with_duplicate_email()
    {
        $existing = User::factory()->create(['email' => 'exists@example.com']);
        
        $response = $this->postJson('/api/users', [
            'name' => 'Jane',
            'email' => 'exists@example.com',
            'password' => 'password123',
            'password_confirmation' => 'password123',
        ]);
        
        $response->assertStatus(422)
            ->assertJsonValidationErrors(['email']);
    }
    
    public function test_requires_authentication_to_update_user()
    {
        $user = User::factory()->create();
        
        $response = $this->putJson("/api/users/{$user->id}", [
            'name' => 'Updated Name'
        ]);
        
        $response->assertStatus(401);
    }
    
    public function test_user_can_update_own_profile()
    {
        $user = User::factory()->create();
        
        $response = $this->actingAs($user)
            ->putJson("/api/users/{$user->id}", [
                'name' => 'Updated Name'
            ]);
        
        $response->assertStatus(200);
        
        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'Updated Name'
        ]);
    }
}
```

---

### Q15: Unit Tests and Mocking

```php
// tests/Unit/UserServiceTest.php
namespace Tests\Unit;

use App\Models\User;
use App\Repositories\UserRepository;
use App\Services\UserService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class UserServiceTest extends TestCase
{
    use RefreshDatabase;
    
    public function test_creates_user_with_hashed_password()
    {
        $repository = new UserRepository();
        $service = new UserService($repository);
        
        $user = $service->createUser([
            'name' => 'John',
            'email' => 'john@example.com',
            'password' => 'plaintext'
        ]);
        
        $this->assertNotEquals('plaintext', $user->password);
        $this->assertTrue(Hash::check('plaintext', $user->password));
    }
    
    public function test_creates_user_with_mocked_repository()
    {
        $mockRepo = $this->createMock(UserRepository::class);
        
        $mockRepo->expects($this->once())
            ->method('create')
            ->with($this->callback(function ($data) {
                return $data['email'] === 'test@example.com';
            }))
            ->willReturn(new User([
                'id' => 1,
                'name' => 'Test',
                'email' => 'test@example.com'
            ]));
        
        $service = new UserService($mockRepo);
        $user = $service->createUser([
            'name' => 'Test',
            'email' => 'test@example.com',
            'password' => 'password'
        ]);
        
        $this->assertEquals('test@example.com', $user->email);
    }
}

// Faking
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Queue;
use Illuminate\Support\Facades\Event;

public function test_sends_welcome_email()
{
    Mail::fake();
    
    $user = User::factory()->create();
    
    $user->sendWelcomeEmail();
    
    Mail::assertSent(WelcomeMail::class, function ($mail) use ($user) {
        return $mail->user->id === $user->id;
    });
}

public function test_dispatches_job()
{
    Queue::fake();
    
    ProcessOrder::dispatch($order);
    
    Queue::assertPushed(ProcessOrder::class);
}

public function test_event_dispatched()
{
    Event::fake();
    
    OrderPlaced::dispatch($order);
    
    Event::assertDispatched(OrderPlaced::class);
}
```

---

## 10. Performance & Caching

### Q16: Caching Strategies

```php
use Illuminate\Support\Facades\Cache;

// Remember pattern
$users = Cache::remember('users.all', 3600, function () {
    return User::all();
});

// Remember forever
$config = Cache::rememberForever('site.config', function () {
    return Config::all();
});

// Forget cache
Cache::forget('users.all');

// Tagged cache (Redis/Memcached only)
Cache::tags(['users', 'admin'])->put('key', $value, 3600);
Cache::tags(['users'])->flush();

// Model caching
class User extends Model
{
    public static function boot()
    {
        parent::boot();
        
        static::updated(function ($user) {
            Cache::forget("user.{$user->id}");
        });
        
        static::deleted(function ($user) {
            Cache::forget("user.{$user->id}");
        });
    }
    
    public static function findCached($id)
    {
        return Cache::remember("user.{$id}", 3600, function () use ($id) {
            return static::find($id);
        });
    }
}

// Query result caching
$users = DB::table('users')
    ->where('active', true)
    ->remember(3600)
    ->get();

// Cache lock (prevent race conditions)
$lock = Cache::lock('process-orders', 10);

if ($lock->get()) {
    // Process orders (max 10 seconds)
    
    $lock->release();
}

// Or use closure
Cache::lock('process-orders')->get(function () {
    // Process orders
});

// Response caching
Route::get('/posts', function () {
    return Cache::remember('posts.index', 3600, function () {
        return Post::with('user')->latest()->get();
    });
});

// Middleware caching
Route::middleware('cache.headers:public;max_age=3600')->group(function () {
    Route::get('/api/posts', [PostController::class, 'index']);
});
```

---

### Q17: Database Query Optimization

```php
// Use select to limit columns
$users = User::select(['id', 'name', 'email'])->get();

// Chunking for large datasets
User::chunk(100, function ($users) {
    foreach ($users as $user) {
        // Process
    }
});

// Lazy collections
User::lazy()->each(function ($user) {
    // Memory efficient
});

// Index hints
User::from('users USE INDEX (idx_email)')
    ->where('email', 'test@example.com')
    ->first();

// Specific eager loading
$users = User::with(['posts' => function ($query) {
    $query->select('id', 'user_id', 'title')
        ->where('published', true);
}])->get();

// Load count without loading relation
$users = User::withCount('posts')->get();

// Exists queries (faster than count)
if (Post::where('user_id', $userId)->exists()) {
    // Has posts
}

// Cursor pagination (memory efficient)
foreach (User::cursor() as $user) {
    // Process
}

// Database indexing
Schema::table('users', function (Blueprint $table) {
    $table->index('email');
    $table->index(['status', 'created_at']);
    $table->unique('username');
});

// Query optimization
// Bad
$users = User::all();
foreach ($users as $user) {
    $user->posts; // N+1 problem
}

// Good
$users = User::with('posts')->get();
foreach ($users as $user) {
    $user->posts; // Pre-loaded
}
```

---

## 🎯 Laravel Best Practices Summary

### Architecture
✅ Use Repository pattern for complex data access  
✅ Implement Service layer for business logic  
✅ Use Form Requests for validation  
✅ Leverage API Resources for response formatting  
✅ Follow SOLID principles

### Performance
✅ Eager load relationships (avoid N+1)  
✅ Use query scopes and indexes  
✅ Implement caching strategically  
✅ Use queues for long-running tasks  
✅ Optimize database queries with EXPLAIN

### Security
✅ Use Laravel Sanctum/Passport for API auth  
✅ Implement Gates and Policies  
✅ Validate all inputs with Form Requests  
✅ Use parameterized queries (Eloquent does this)  
✅ Enable CSRF protection

### Code Quality
✅ Write tests (Feature + Unit)  
✅ Use type hints and return types  
✅ Follow PSR standards  
✅ Use Events for decoupling  
✅ Keep controllers thin

### Production
✅ Use environment variables (.env)  
✅ Enable route caching  
✅ Enable config caching  
✅ Use Horizon for queue monitoring  
✅ Implement logging and monitoring

---

**Interview Tips:**
1. **Know the lifecycle** - Understand request → kernel → middleware → controller → response
2. **Explain trade-offs** - "Repositories add abstraction but may be overkill for small apps"
3. **Discuss scaling** - How would you handle 10k concurrent users?
4. **Real examples** - Share actual Laravel projects you've built
5. **Latest features** - Know what's new in Laravel 11

Good luck with your Laravel interviews! 🚀
