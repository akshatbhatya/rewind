# ⚡ JavaScript Best Practices & Interview Guide (3 Years Experience)

**Focus:** Modern JavaScript (ES6+), Async Programming, Design Patterns, Performance, and Clean Code

This comprehensive guide contains **35+ JavaScript interview questions** with production-ready code examples and industry best practices.

---

## Table of Contents
1. [Core JavaScript Concepts](#1-core-javascript-concepts)
2. [ES6+ Features](#2-es6-features)
3. [Async Programming](#3-async-programming)
4. [Functional Programming](#4-functional-programming)
5. [Object-Oriented JavaScript](#5-object-oriented-javascript)
6. [Design Patterns](#6-design-patterns)
7. [Performance Optimization](#7-performance-optimization)
8. [Security Best Practices](#8-security-best-practices)
9. [Testing & Quality](#9-testing--quality)
10. [Modern Tools & Ecosystem](#10-modern-tools--ecosystem)

---

## 1. Core JavaScript Concepts

### Q1: Explain Closures with Practical Examples

**What is a Closure?**
A closure is a function that has access to variables in its outer (enclosing) function's scope, even after the outer function has returned.

```javascript
// Basic closure example
function createCounter() {
    let count = 0; // Private variable
    
    return {
        increment() {
            count++;
            return count;
        },
        decrement() {
            count--;
            return count;
        },
        getCount() {
            return count;
        }
    };
}

const counter = createCounter();
console.log(counter.increment()); // 1
console.log(counter.increment()); // 2
console.log(counter.getCount());  // 2
console.log(counter.count);       // undefined (private!)

// Practical use case: Module pattern
const userModule = (function() {
    // Private variables
    const users = [];
    let nextId = 1;
    
    // Private function
    function validateUser(user) {
        return user.name && user.email;
    }
    
    // Public API
    return {
        addUser(name, email) {
            const user = { id: nextId++, name, email };
            if (validateUser(user)) {
                users.push(user);
                return user;
            }
            throw new Error('Invalid user data');
        },
        
        getUser(id) {
            return users.find(u => u.id === id);
        },
        
        getAllUsers() {
            return [...users]; // Return copy, not reference
        }
    };
})();

userModule.addUser('John Doe', 'john@example.com');
console.log(userModule.getAllUsers());
console.log(userModule.users); // undefined - private!

// Common pitfall: Loop with closure
// Bad
for (var i = 0; i < 3; i++) {
    setTimeout(function() {
        console.log(i); // Prints: 3, 3, 3
    }, 1000);
}

// Good: Use let (block scope)
for (let i = 0; i < 3; i++) {
    setTimeout(function() {
        console.log(i); // Prints: 0, 1, 2
    }, 1000);
}

// Good: Use IIFE
for (var i = 0; i < 3; i++) {
    (function(j) {
        setTimeout(function() {
            console.log(j); // Prints: 0, 1, 2
        }, 1000);
    })(i);
}
```

**Best Practices:**
✅ Use closures for data privacy  
✅ Avoid memory leaks (don't create unnecessary closures)  
✅ Use `let`/`const` instead of `var`

---

### Q2: `this` Keyword - All Context Scenarios

```javascript
// 1. Global context
console.log(this); // window (browser) or global (Node.js)

function globalFunction() {
    console.log(this); // window in non-strict, undefined in strict mode
}

// 2. Object method
const person = {
    name: 'John',
    greet() {
        console.log(this.name); // 'John'
    },
    greetArrow: () => {
        console.log(this.name); // undefined (arrow functions don't have their own 'this')
    }
};

person.greet(); // 'John'
person.greetArrow(); // undefined

// 3. Constructor function
function User(name) {
    this.name = name;
    this.getName = function() {
        return this.name;
    };
}

const user = new User('Alice');
console.log(user.getName()); // 'Alice'

// 4. Class context
class Person {
    constructor(name) {
        this.name = name;
    }
    
    greet() {
        console.log(`Hello, I'm ${this.name}`);
    }
    
    // Arrow function as class field (maintains 'this')
    greetAsync = () => {
        setTimeout(() => {
            console.log(`Hello from ${this.name}`);
        }, 1000);
    }
}

const person1 = new Person('Bob');
person1.greet(); // Works

const greetMethod = person1.greet;
greetMethod(); // Error: 'this' is undefined

// 5. Explicit binding
const person2 = { name: 'Charlie' };
person.greet.call(person2);  // 'Charlie'
person.greet.apply(person2); // 'Charlie'
const boundGreet = person.greet.bind(person2);
boundGreet(); // 'Charlie'

// 6. Event handlers
button.addEventListener('click', function() {
    console.log(this); // The button element
});

button.addEventListener('click', () => {
    console.log(this); // Lexical 'this' (outer scope)
});

// Best practice: Binding in React
class MyComponent extends React.Component {
    constructor(props) {
        super(props);
        this.handleClick = this.handleClick.bind(this);
    }
    
    handleClick() {
        console.log(this.props); // Works
    }
    
    // Or use arrow function
    handleClickArrow = () => {
        console.log(this.props); // Works
    }
}
```

---

### Q3: Prototypal Inheritance vs Class-based

```javascript
// Prototype chain
function Animal(name) {
    this.name = name;
}

Animal.prototype.speak = function() {
    console.log(`${this.name} makes a sound`);
};

function Dog(name, breed) {
    Animal.call(this, name); // Call parent constructor
    this.breed = breed;
}

// Set up inheritance
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;

Dog.prototype.speak = function() {
    console.log(`${this.name} barks`);
};

const dog = new Dog('Rex', 'German Shepherd');
dog.speak(); // 'Rex barks'
console.log(dog instanceof Dog);    // true
console.log(dog instanceof Animal); // true

// ES6 Classes (syntactic sugar over prototypes)
class AnimalES6 {
    constructor(name) {
        this.name = name;
    }
    
    speak() {
        console.log(`${this.name} makes a sound`);
    }
    
    static create(name) {
        return new AnimalES6(name);
    }
}

class DogES6 extends AnimalES6 {
    constructor(name, breed) {
        super(name);
        this.breed = breed;
    }
    
    speak() {
        console.log(`${this.name} barks`);
    }
    
    fetch() {
        console.log(`${this.name} fetches the ball`);
    }
}

const dog2 = new DogES6('Max', 'Labrador');
dog2.speak(); // 'Max barks'
dog2.fetch(); // 'Max fetches the ball'

// Check prototype chain
console.log(Object.getPrototypeOf(dog2) === DogES6.prototype); // true
console.log(Object.getPrototypeOf(DogES6.prototype) === AnimalES6.prototype); // true
```

---

### Q4: Event Loop, Call Stack, and Task Queue

```javascript
console.log('1'); // Synchronous

setTimeout(() => {
    console.log('2'); // Macrotask
}, 0);

Promise.resolve().then(() => {
    console.log('3'); // Microtask
});

console.log('4'); // Synchronous

// Output: 1, 4, 3, 2

// Detailed example
console.log('Start');

setTimeout(() => {
    console.log('Timeout 1');
}, 0);

Promise.resolve()
    .then(() => console.log('Promise 1'))
    .then(() => console.log('Promise 2'));

setTimeout(() => {
    console.log('Timeout 2');
    Promise.resolve().then(() => console.log('Promise 3'));
}, 0);

console.log('End');

/* Output:
Start
End
Promise 1
Promise 2
Timeout 1
Timeout 2
Promise 3
*/

// Visualization of execution order
/**
 * Call Stack: LIFO
 * Microtask Queue: Promises, MutationObserver (higher priority)
 * Macrotask Queue: setTimeout, setInterval, I/O
 * 
 * Execution order:
 * 1. Execute all synchronous code
 * 2. Execute all microtasks
 * 3. Execute one macrotask
 * 4. Execute all microtasks again
 * 5. Repeat from step 3
 */

// Common pitfall: Blocking event loop
// Bad
function blockingOperation() {
    const start = Date.now();
    while (Date.now() - start < 3000) {
        // Blocks for 3 seconds!
    }
}

// Good: Use async operations
async function nonBlockingOperation() {
    await new Promise(resolve => setTimeout(resolve, 3000));
}
```

---

## 2. ES6+ Features

### Q5: Destructuring - Advanced Patterns

```javascript
// Array destructuring
const [first, second, ...rest] = [1, 2, 3, 4, 5];
console.log(first);  // 1
console.log(second); // 2
console.log(rest);   // [3, 4, 5]

// Skip elements
const [a, , c] = [1, 2, 3];
console.log(a, c); // 1, 3

// Default values
const [x = 0, y = 0] = [1];
console.log(x, y); // 1, 0

// Object destructuring
const user = {
    id: 1,
    name: 'John',
    email: 'john@example.com',
    address: {
        city: 'New York',
        zip: '10001'
    }
};

const { name, email, phone = 'N/A' } = user;
console.log(name, email, phone); // 'John', 'john@example.com', 'N/A'

// Rename variables
const { name: username, email: userEmail } = user;
console.log(username, userEmail);

// Nested destructuring
const { address: { city, zip } } = user;
console.log(city, zip); // 'New York', '10001'

// Function parameters
function createUser({ name, email, role = 'user' }) {
    return { name, email, role, createdAt: Date.now() };
}

const newUser = createUser({ name: 'Alice', email: 'alice@example.com' });

// Swapping variables
let m = 1, n = 2;
[m, n] = [n, m];
console.log(m, n); // 2, 1

// Rest in objects
const { id, ...userWithoutId } = user;
console.log(userWithoutId); // { name: '...', email: '...', address: {...} }

// Practical use case: API response handling
async function fetchUser(id) {
    const response = await fetch(`/api/users/${id}`);
    const { 
        data: { 
            user: { name, email },
            permissions 
        },
        meta: { timestamp }
    } = await response.json();
    
    return { name, email, permissions, timestamp };
}
```

---

### Q6: Spread and Rest Operators

```javascript
// Spread in arrays
const arr1 = [1, 2, 3];
const arr2 = [4, 5, 6];
const combined = [...arr1, ...arr2];
console.log(combined); // [1, 2, 3, 4, 5, 6]

// Clone array (shallow copy)
const original = [1, 2, 3];
const clone = [...original];

// Spread in objects
const obj1 = { a: 1, b: 2 };
const obj2 = { c: 3, d: 4 };
const merged = { ...obj1, ...obj2 };
console.log(merged); // { a: 1, b: 2, c: 3, d: 4 }

// Override properties
const defaults = { theme: 'light', lang: 'en' };
const userPrefs = { theme: 'dark' };
const config = { ...defaults, ...userPrefs };
console.log(config); // { theme: 'dark', lang: 'en' }

// Rest parameters
function sum(...numbers) {
    return numbers.reduce((acc, num) => acc + num, 0);
}

console.log(sum(1, 2, 3, 4)); // 10

// Practical examples
class ApiClient {
    constructor(baseURL, defaultHeaders = {}) {
        this.baseURL = baseURL;
        this.defaultHeaders = defaultHeaders;
    }
    
    async request(endpoint, { headers = {}, ...options } = {}) {
        const response = await fetch(`${this.baseURL}${endpoint}`, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...this.defaultHeaders,
                ...headers
            }
        });
        return response.json();
    }
}

// Immutable state updates (React/Redux pattern)
const state = {
    user: { name: 'John', age: 30 },
    posts: [1, 2, 3]
};

// Update nested object
const newState = {
    ...state,
    user: {
        ...state.user,
        age: 31
    }
};

// Add to array
const withNewPost = {
    ...state,
    posts: [...state.posts, 4]
};

// Remove from array
const withoutFirstPost = {
    ...state,
    posts: state.posts.filter((_, i) => i !== 0)
};

// Deep clone limitation (only shallow!)
const nested = { a: { b: 1 } };
const shallowClone = { ...nested };
shallowClone.a.b = 2;
console.log(nested.a.b); // 2 (mutated!)

// For deep clone, use:
const deepClone = JSON.parse(JSON.stringify(nested));
// Or lodash cloneDeep
// Or structuredClone (modern browsers)
```

---

### Q7: Template Literals and Tagged Templates

```javascript
// Basic template literal
const name = 'John';
const age = 30;
const message = `Hello, my name is ${name} and I'm ${age} years old.`;

// Multi-line strings
const html = `
    <div class="card">
        <h2>${name}</h2>
        <p>Age: ${age}</p>
    </div>
`;

// Expression evaluation
const price = 19.99;
const quantity = 3;
console.log(`Total: $${(price * quantity).toFixed(2)}`);

// Tagged templates
function highlight(strings, ...values) {
    return strings.reduce((acc, str, i) => {
        const value = values[i] ? `<mark>${values[i]}</mark>` : '';
        return acc + str + value;
    }, '');
}

const highlighted = highlight`Name: ${name}, Age: ${age}`;
console.log(highlighted);
// "Name: <mark>John</mark>, Age: <mark>30</mark>"

// SQL query builder (safe)
function sql(strings, ...values) {
    return {
        text: strings.reduce((acc, str, i) => {
            return acc + str + (i < values.length ? `$${i + 1}` : '');
        }, ''),
        values: values
    };
}

const userId = 123;
const query = sql`SELECT * FROM users WHERE id = ${userId}`;
console.log(query);
// { text: 'SELECT * FROM users WHERE id = $1', values: [123] }

// Styled components pattern
function css(strings, ...values) {
    return strings.reduce((acc, str, i) => {
        const value = values[i] || '';
        return acc + str + value;
    }, '');
}

const primaryColor = '#007bff';
const styles = css`
    .button {
        background-color: ${primaryColor};
        padding: 10px 20px;
    }
`;
```

---

### Q8: Symbols and Well-known Symbols

```javascript
// Create unique symbols
const id = Symbol('id');
const anotherId = Symbol('id');
console.log(id === anotherId); // false - each symbol is unique

// Use as object keys
const user = {
    name: 'John',
    [id]: 123
};

console.log(user[id]); // 123
console.log(Object.keys(user)); // ['name'] - symbols not enumerable

// Private properties pattern
const _private = Symbol('private');

class BankAccount {
    constructor(balance) {
        this[_private] = { balance };
    }
    
    getBalance() {
        return this[_private].balance;
    }
    
    deposit(amount) {
        this[_private].balance += amount;
    }
}

const account = new BankAccount(1000);
console.log(account.getBalance()); // 1000
console.log(account[_private]); // undefined (can't access without the symbol)

// Well-known symbols
class Collection {
    constructor(items = []) {
        this.items = items;
    }
    
    // Make iterable
    *[Symbol.iterator]() {
        for (const item of this.items) {
            yield item;
        }
    }
    
    // Custom toString
    [Symbol.toStringTag]() {
        return 'Collection';
    }
}

const collection = new Collection([1, 2, 3]);

for (const item of collection) {
    console.log(item); // 1, 2, 3
}

console.log(Object.prototype.toString.call(collection));
// [object Collection]

// Symbol.hasInstance
class MyArray {
    static [Symbol.hasInstance](instance) {
        return Array.isArray(instance);
    }
}

console.log([] instanceof MyArray); // true
```

---

## 3. Async Programming

### Q9: Promises - Best Practices

```javascript
// Creating promises
function fetchUser(id) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (id > 0) {
                resolve({ id, name: 'John' });
            } else {
                reject(new Error('Invalid ID'));
            }
        }, 1000);
    });
}

// Consuming promises
fetchUser(1)
    .then(user => {
        console.log(user);
        return fetchUser(2); // Chain promises
    })
    .then(user => console.log(user))
    .catch(error => console.error(error))
    .finally(() => console.log('Done'));

// Promise.all - Wait for all (fails fast)
Promise.all([
    fetchUser(1),
    fetchUser(2),
    fetchUser(3)
]).then(users => {
    console.log(users); // Array of results
}).catch(error => {
    console.error('One failed:', error);
});

// Promise.allSettled - Wait for all (doesn't fail fast)
Promise.allSettled([
    fetchUser(1),
    fetchUser(-1), // This will reject
    fetchUser(2)
]).then(results => {
    results.forEach(result => {
        if (result.status === 'fulfilled') {
            console.log('Success:', result.value);
        } else {
            console.log('Failed:', result.reason);
        }
    });
});

// Promise.race - First to complete
Promise.race([
    fetchUser(1),
    fetchUser(2)
]).then(user => {
    console.log('First:', user);
});

// Promise.any - First to fulfill (ignores rejections)
Promise.any([
    Promise.reject('Error 1'),
    fetchUser(2),
    fetchUser(3)
]).then(user => {
    console.log('First success:', user);
});

// Error handling best practices
function robustFetch(url) {
    return fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            return response.json();
        })
        .catch(error => {
            console.error('Fetch failed:', error);
            // Return default or rethrow
            throw error;
        });
}

// Avoid promise constructor antipattern
// Bad
function badAsync() {
    return new Promise((resolve, reject) => {
        someAsyncFunction()
            .then(result => resolve(result))
            .catch(error => reject(error));
    });
}

// Good
function goodAsync() {
    return someAsyncFunction();
}

// Parallel execution
async function fetchAllUsers() {
    const ids = [1, 2, 3, 4, 5];
    
    // Sequential (slow)
    const usersSeq = [];
    for (const id of ids) {
        const user = await fetchUser(id);
        usersSeq.push(user);
    }
    
    // Parallel (fast)
    const usersPar = await Promise.all(
        ids.map(id => fetchUser(id))
    );
    
    return usersPar;
}
```

---

### Q10: Async/Await - Advanced Patterns

```javascript
// Basic async/await
async function getUser(id) {
    try {
        const response = await fetch(`/api/users/${id}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const user = await response.json();
        return user;
    } catch (error) {
        console.error('Failed to fetch user:', error);
        throw error;
    }
}

// Sequential vs Parallel
async function example() {
    // Sequential (3 seconds total)
    const user1 = await fetchUser(1); // 1s
    const user2 = await fetchUser(2); // 1s
    const user3 = await fetchUser(3); // 1s
    
    // Parallel (1 second total)
    const [user4, user5, user6] = await Promise.all([
        fetchUser(4),
        fetchUser(5),
        fetchUser(6)
    ]);
}

// Error handling patterns
async function fetchWithRetry(url, retries = 3) {
    for (let i = 0; i < retries; i++) {
        try {
            const response = await fetch(url);
            return await response.json();
        } catch (error) {
            if (i === retries - 1) throw error;
            await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
        }
    }
}

// Async iteration
async function* fetchPaginated(baseUrl) {
    let page = 1;
    while (true) {
        const response = await fetch(`${baseUrl}?page=${page}`);
        const data = await response.json();
        
        if (data.items.length === 0) break;
        
        yield* data.items;
        page++;
    }
}

// Usage
async function processAllItems() {
    for await (const item of fetchPaginated('/api/items')) {
        console.log(item);
    }
}

// Parallel execution with limit
async function parallelWithLimit(tasks, limit) {
    const results = [];
    const executing = [];
    
    for (const task of tasks) {
        const promise = Promise.resolve().then(() => task());
        results.push(promise);
        
        if (limit <= tasks.length) {
            const e = promise.then(() => 
                executing.splice(executing.indexOf(e), 1)
            );
            executing.push(e);
            
            if (executing.length >= limit) {
                await Promise.race(executing);
            }
        }
    }
    
    return Promise.all(results);
}

// Usage: Process 100 tasks with max 5 concurrent
const tasks = Array.from({ length: 100 }, (_, i) => 
    () => fetchUser(i)
);
await parallelWithLimit(tasks, 5);

// Timeout wrapper
function withTimeout(promise, ms) {
    return Promise.race([
        promise,
        new Promise((_, reject) =>
            setTimeout(() => reject(new Error('Timeout')), ms)
        )
    ]);
}

// Usage
try {
    const user = await withTimeout(fetchUser(1), 5000);
} catch (error) {
    console.error('Request timed out or failed');
}
```

---

## 4. Functional Programming

### Q11: Pure Functions and Immutability

```javascript
// Pure functions
// Bad: Impure (side effects, external state)
let total = 0;
function addToTotal(value) {
    total += value; // Mutates external state
    return total;
}

// Good: Pure
function add(a, b) {
    return a + b; // No side effects
}

// Immutable data operations
const numbers = [1, 2, 3, 4, 5];

// Bad: Mutates original
numbers.push(6);
numbers.sort((a, b) => b - a);

// Good: Returns new array
const withSix = [...numbers, 6];
const sorted = [...numbers].sort((a, b) => b - a);

// Object immutability
const user = { name: 'John', age: 30 };

// Bad
user.age = 31;

// Good
const updatedUser = { ...user, age: 31 };

// Deep updates
const state = {
    user: {
        profile: {
            name: 'John',
            settings: { theme: 'light' }
        }
    }
};

// Update nested property immutably
const newState = {
    ...state,
    user: {
        ...state.user,
        profile: {
            ...state.user.profile,
            settings: {
                ...state.user.profile.settings,
                theme: 'dark'
            }
        }
    }
};

// Helper for deep updates (Immer-like)
function produce(state, recipe) {
    const draft = JSON.parse(JSON.stringify(state));
    recipe(draft);
    return draft;
}

const result = produce(state, draft => {
    draft.user.profile.settings.theme = 'dark';
});
```

---

### Q12: Higher-Order Functions

```javascript
// Functions that take functions as arguments or return functions

// Map, Filter, Reduce
const numbers = [1, 2, 3, 4, 5];

const doubled = numbers.map(n => n * 2);
const evens = numbers.filter(n => n % 2 === 0);
const sum = numbers.reduce((acc, n) => acc + n, 0);

// Function composition
const compose = (...fns) => x => 
    fns.reduceRight((acc, fn) => fn(acc), x);

const pipe = (...fns) => x =>
    fns.reduce((acc, fn) => fn(acc), x);

const addOne = x => x + 1;
const double = x => x * 2;
const square = x => x * x;

const composedFn = compose(square, double, addOne);
console.log(composedFn(2)); // square(double(addOne(2))) = square(6) = 36

const pipedFn = pipe(addOne, double, square);
console.log(pipedFn(2)); // square(double(addOne(2))) = square(6) = 36

// Currying
function curry(fn) {
    return function curried(...args) {
        if (args.length >= fn.length) {
            return fn.apply(this, args);
        }
        return function(...nextArgs) {
            return curried.apply(this, [...args, ...nextArgs]);
        };
    };
}

const sum3 = (a, b, c) => a + b + c;
const curriedSum = curry(sum3);

console.log(curriedSum(1)(2)(3));     // 6
console.log(curriedSum(1, 2)(3));     // 6
console.log(curriedSum(1)(2, 3));     // 6

// Practical use: Partial application
const multiply = (a, b) => a * b;
const double2 = multiply.bind(null, 2);
console.log(double2(5)); // 10

// Memoization
function memoize(fn) {
    const cache = new Map();
    return function(...args) {
        const key = JSON.stringify(args);
        if (cache.has(key)) {
            return cache.get(key);
        }
        const result = fn.apply(this, args);
        cache.set(key, result);
        return result;
    };
}

const fibonacci = memoize(function(n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);
});

console.log(fibonacci(40)); // Fast!

// Debounce and Throttle
function debounce(fn, delay) {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

function throttle(fn, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            fn.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// Usage
const handleSearch = debounce((query) => {
    console.log('Searching for:', query);
}, 300);

const handleScroll = throttle(() => {
    console.log('Scrolling...');
}, 100);
```

---

### Q13: Array Methods - Advanced Usage

```javascript
const users = [
    { id: 1, name: 'John', age: 30, active: true },
    { id: 2, name: 'Jane', age: 25, active: false },
    { id: 3, name: 'Bob', age: 35, active: true }
];

// Map
const names = users.map(user => user.name);
const userSummaries = users.map(({ id, name }) => ({ id, name }));

// Filter
const activeUsers = users.filter(user => user.active);
const adults = users.filter(user => user.age >= 18);

// Find
const john = users.find(user => user.name === 'John');
const youngIndex = users.findIndex(user => user.age < 26);

// Reduce (powerful!)
const totalAge = users.reduce((sum, user) => sum + user.age, 0);

const usersById = users.reduce((acc, user) => {
    acc[user.id] = user;
    return acc;
}, {});

const grouped = users.reduce((acc, user) => {
    const key = user.active ? 'active' : 'inactive';
    if (!acc[key]) acc[key] = [];
    acc[key].push(user);
    return acc;
}, {});

// Every and Some
const allActive = users.every(user => user.active); // false
const someActive = users.some(user => user.active); // true

// Flat and FlatMap
const nested = [[1, 2], [3, 4], [5]];
const flattened = nested.flat(); // [1, 2, 3, 4, 5]

const deepNested = [[[1]], [[2]], [[3]]];
const deepFlat = deepNested.flat(2); // [1, 2, 3]

const doubled = users.flatMap(user => [user, { ...user, id: user.id + 100 }]);

// Sort (mutates! Use with spread)
const byAge = [...users].sort((a, b) => a.age - b.age);
const byName = [...users].sort((a, b) => a.name.localeCompare(b.name));

// Chaining
const result = users
    .filter(user => user.active)
    .map(user => ({ ...user, ageInMonths: user.age * 12 }))
    .sort((a, b) => b.age - a.age)
    .slice(0, 2);

// Advanced: Group by (native in newer JS)
const groupBy = (arr, key) => 
    arr.reduce((acc, item) => {
        const group = item[key];
        if (!acc[group]) acc[group] = [];
        acc[group].push(item);
        return acc;
    }, {});

const byActive = groupBy(users, 'active');
```

---

## 5. Object-Oriented JavaScript

### Q14: Classes - Modern Patterns

```javascript
// ES6 Class
class User {
    // Private fields (ES2022)
    #password;
    
    // Static property
    static roles = ['admin', 'user', 'guest'];
    
    constructor(name, email, password) {
        this.name = name;
        this.email = email;
        this.#password = password;
        this.createdAt = new Date();
    }
    
    // Public method
    getInfo() {
        return `${this.name} (${this.email})`;
    }
    
    // Private method
    #hashPassword(password) {
        return btoa(password); // Simple encoding (use bcrypt in production!)
    }
    
    // Getter
    get password() {
        return '***hidden***';
    }
    
    // Setter
    set password(newPassword) {
        if (newPassword.length < 8) {
            throw new Error('Password too short');
        }
        this.#password = this.#hashPassword(newPassword);
    }
    
    // Static method
    static create(name, email, password) {
        return new User(name, email, password);
    }
    
    // Method shorthand
    greet() {
        return `Hello, I'm ${this.name}`;
    }
}

const user = new User('John', 'john@example.com', 'password123');
console.log(user.getInfo());
console.log(user.password); // '***hidden***'
// console.log(user.#password); // SyntaxError: Private field

// Inheritance
class Admin extends User {
    constructor(name, email, password, permissions = []) {
        super(name, email, password);
        this.permissions = permissions;
    }
    
    hasPermission(perm) {
        return this.permissions.includes(perm);
    }
    
    // Override
    greet() {
        return `${super.greet()} (Admin)`;
    }
}

const admin = new Admin('Alice', 'alice@example.com', 'admin123', ['read', 'write']);
console.log(admin.greet());
console.log(admin.hasPermission('write')); // true

// Mixins
const TimestampMixin = {
    setCreatedAt() {
        this.createdAt = new Date();
    },
    setUpdatedAt() {
        this.updatedAt = new Date();
    }
};

const ValidationMixin = {
    validate() {
        return Object.keys(this).every(key => this[key] != null);
    }
};

function applyMixins(targetClass, ...mixins) {
    mixins.forEach(mixin => {
        Object.assign(targetClass.prototype, mixin);
    });
}

class Product {
    constructor(name, price) {
        this.name = name;
        this.price = price;
    }
}

applyMixins(Product, TimestampMixin, ValidationMixin);

const product = new Product('Laptop', 999);
product.setCreatedAt();
console.log(product.validate()); // true
```

---

## 6. Design Patterns

### Q15: Singleton Pattern

```javascript
// Classic singleton
class Database {
    constructor() {
        if (Database.instance) {
            return Database.instance;
        }
        this.connection = null;
        Database.instance = this;
    }
    
    connect() {
        if (!this.connection) {
            this.connection = { status: 'connected' };
        }
        return this.connection;
    }
}

const db1 = new Database();
const db2 = new Database();
console.log(db1 === db2); // true

// Module pattern (better)
const ConfigManager = (function() {
    let instance;
    let config = {};
    
    function createInstance() {
        return {
            get(key) {
                return config[key];
            },
            set(key, value) {
                config[key] = value;
            },
            getAll() {
                return { ...config };
            }
        };
    }
    
    return {
        getInstance() {
            if (!instance) {
                instance = createInstance();
            }
            return instance;
        }
    };
})();

const config1 = ConfigManager.getInstance();
const config2 = ConfigManager.getInstance();
config1.set('theme', 'dark');
console.log(config2.get('theme')); // 'dark'
```

---

### Q16: Factory Pattern

```javascript
class Car {
    constructor(options) {
        this.doors = options.doors || 4;
        this.state = options.state || 'new';
        this.color = options.color || 'black';
    }
}

class Truck {
    constructor(options) {
        this.doors = options.doors || 2;
        this.state = options.state || 'new';
        this.wheelSize = options.wheelSize || 'large';
    }
}

class VehicleFactory {
    createVehicle(type, options) {
        switch(type) {
            case 'car':
                return new Car(options);
            case 'truck':
                return new Truck(options);
            default:
                throw new Error('Unknown vehicle type');
        }
    }
}

const factory = new VehicleFactory();
const car = factory.createVehicle('car', { color: 'red', doors: 2 });
const truck = factory.createVehicle('truck', { wheelSize: 'huge' });

// Abstract factory
class Button {
    render() {
        throw new Error('Must implement render');
    }
}

class WindowsButton extends Button {
    render() {
        return '<button class="windows">Click me</button>';
    }
}

class MacButton extends Button {
    render() {
        return '<button class="mac">Click me</button>';
    }
}

class UIFactory {
    createButton() {
        throw new Error('Must implement createButton');
    }
}

class WindowsFactory extends UIFactory {
    createButton() {
        return new WindowsButton();
    }
}

class MacFactory extends UIFactory {
    createButton() {
        return new MacButton();
    }
}

// Usage
function createUI(os) {
    const factory = os === 'windows' 
        ? new WindowsFactory() 
        : new MacFactory();
    
    const button = factory.createButton();
    return button.render();
}
```

---

### Q17: Observer Pattern (Pub/Sub)

```javascript
class EventEmitter {
    constructor() {
        this.events = {};
    }
    
    on(event, callback) {
        if (!this.events[event]) {
            this.events[event] = [];
        }
        this.events[event].push(callback);
        
        // Return unsubscribe function
        return () => this.off(event, callback);
    }
    
    off(event, callback) {
        if (!this.events[event]) return;
        
        this.events[event] = this.events[event].filter(
            cb => cb !== callback
        );
    }
    
    emit(event, ...args) {
        if (!this.events[event]) return;
        
        this.events[event].forEach(callback => {
            callback(...args);
        });
    }
    
    once(event, callback) {
        const wrapper = (...args) => {
            callback(...args);
            this.off(event, wrapper);
        };
        this.on(event, wrapper);
    }
}

// Usage
const emitter = new EventEmitter();

const unsubscribe = emitter.on('user:login', (user) => {
    console.log('User logged in:', user.name);
});

emitter.on('user:login', (user) => {
    console.log('Send welcome email to:', user.email);
});

emitter.once('app:ready', () => {
    console.log('App initialized');
});

emitter.emit('user:login', { name: 'John', email: 'john@example.com' });
emitter.emit('app:ready');
emitter.emit('app:ready'); // Won't trigger (once)

// Unsubscribe
unsubscribe();
```

---

## 7. Performance Optimization

### Q18: Lazy Loading and Code Splitting

```javascript
// Dynamic imports
async function loadModule() {
    try {
        const module = await import('./heavy-module.js');
        module.default();
    } catch (error) {
        console.error('Failed to load module:', error);
    }
}

// Lazy load on user interaction
button.addEventListener('click', async () => {
    const { Chart } = await import('chart.js');
    new Chart(ctx, config);
});

// React lazy loading
/*
const HeavyComponent = React.lazy(() => import('./HeavyComponent'));

function App() {
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <HeavyComponent />
        </Suspense>
    );
}
*/

// Image lazy loading
const images = document.querySelectorAll('img[data-src]');

const imageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const img = entry.target;
            img.src = img.dataset.src;
            img.removeAttribute('data-src');
            imageObserver.unobserve(img);
        }
    });
});

images.forEach(img => imageObserver.observe(img));

// Infinite scroll
const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
        loadMoreItems();
    }
}, { threshold: 1.0 });

observer.observe(lastElement);
```

---

### Q19: Memoization and Caching

```javascript
// Simple memoization
function memoize(fn) {
    const cache = new Map();
    
    return function(...args) {
        const key = JSON.stringify(args);
        
        if (cache.has(key)) {
            console.log('Cache hit');
            return cache.get(key);
        }
        
        const result = fn.apply(this, args);
        cache.set(key, result);
        return result;
    };
}

// LRU Cache implementation
class LRUCache {
    constructor(capacity) {
        this.capacity = capacity;
        this.cache = new Map();
    }
    
    get(key) {
        if (!this.cache.has(key)) return -1;
        
        const value = this.cache.get(key);
        // Move to end (most recently used)
        this.cache.delete(key);
        this.cache.set(key, value);
        return value;
    }
    
    put(key, value) {
        if (this.cache.has(key)) {
            this.cache.delete(key);
        }
        
        this.cache.set(key, value);
        
        if (this.cache.size > this.capacity) {
            // Remove least recently used (first item)
            const firstKey = this.cache.keys().next().value;
            this.cache.delete(firstKey);
        }
    }
}

// Usage
const cache = new LRUCache(3);
cache.put('a', 1);
cache.put('b', 2);
cache.put('c', 3);
cache.put('d', 4); // 'a' is evicted
console.log(cache.get('a')); // -1 (not found)

// React useMemo example
/*
function ExpensiveComponent({ data }) {
    const processedData = useMemo(() => {
        return data.map(item => heavyProcessing(item));
    }, [data]);
    
    return <div>{processedData}</div>;
}
*/
```

---

### Q20: Web Workers for Heavy Computation

```javascript
// Main thread
const worker = new Worker('worker.js');

worker.postMessage({ type: 'process', data: largeArray });

worker.onmessage = (event) => {
    console.log('Result from worker:', event.data);
};

worker.onerror = (error) => {
    console.error('Worker error:', error);
};

// worker.js
self.onmessage = (event) => {
    const { type, data } = event.data;
    
    if (type === 'process') {
        const result = heavyComputation(data);
        self.postMessage(result);
    }
};

function heavyComputation(data) {
    // CPU-intensive work
    return data.map(item => /* complex calculation */ item * 2);
}

// Terminate worker when done
worker.terminate();

// Shared Worker (shared across tabs)
const sharedWorker = new SharedWorker('shared-worker.js');

sharedWorker.port.start();
sharedWorker.port.postMessage('Hello');
shared Worker.port.onmessage = (event) => {
    console.log(event.data);
};
```

---

## 8. Security Best Practices

### Q21: XSS Prevention

```javascript
// Bad: Direct HTML insertion (XSS vulnerable)
const userInput = '<img src=x onerror=alert("XSS")>';
element.innerHTML = userInput; // DANGEROUS!

// Good: Escape HTML
function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

element.textContent = userInput; // Safe
element.innerHTML = escapeHtml(userInput); // Safe

// Using DOMPurify library
import DOMPurify from 'dompurify';
const clean = DOMPurify.sanitize(userInput);
element.innerHTML = clean;

// Content Security Policy (CSP)
/*
Add to HTML:
<meta http-equiv="Content-Security-Policy" 
      content="default-src 'self'; script-src 'self'">
*/

// Avoid eval and Function constructor
// Bad
eval('alert("XSS")');
new Function('alert("XSS")')();

// Good: Use safer alternatives
const data = JSON.parse(jsonString);
```

---

### Q22: CSRF Protection

```javascript
// Generate CSRF token
function generateToken() {
    return Array.from(crypto.getRandomValues(new Uint8Array(32)))
        .map(b => b.toString(16).padStart(2, '0'))
        .join('');
}

// Store in cookie and meta tag
const csrfToken = generateToken();
document.cookie = `csrf_token=${csrfToken}; SameSite=Strict; Secure`;
document.querySelector('meta[name="csrf-token"]').content = csrfToken;

// Include in requests
async function secureRequest(url, options = {}) {
    const token = document.querySelector('meta[name="csrf-token"]').content;
    
    return fetch(url, {
        ...options,
        headers: {
            'X-CSRF-Token': token,
            ...options.headers
        }
    });
}

// Axios interceptor
axios.interceptors.request.use(config => {
    const token = document.querySelector('meta[name="csrf-token"]').content;
    config.headers['X-CSRF-Token'] = token;
    return config;
});
```

---

### Q23: Secure Data Storage

```javascript
// Bad: Storing sensitive data in localStorage
localStorage.setItem('password', '123456'); // NEVER!
localStorage.setItem('apiKey', 'secret'); // NEVER!

// Good: Use sessionStorage for temporary data
sessionStorage.setItem('tempData', JSON.stringify(data));

// Encrypt sensitive data before storing
async function encryptData(data, password) {
    const encoder = new TextEncoder();
    const dataBuffer = encoder.encode(data);
    
    const key = await crypto.subtle.importKey(
        'raw',
        encoder.encode(password),
        { name: 'PBKDF2' },
        false,
        ['deriveKey']
    );
    
    const derivedKey = await crypto.subtle.deriveKey(
        {
            name: 'PBKDF2',
            salt: encoder.encode('salt'),
            iterations: 100000,
            hash: 'SHA-256'
        },
        key,
        { name: 'AES-GCM', length: 256 },
        false,
        ['encrypt']
    );
    
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const encrypted = await crypto.subtle.encrypt(
        { name: 'AES-GCM', iv },
        derivedKey,
        dataBuffer
    );
    
    return { encrypted, iv };
}

// Secure cookie options
document.cookie = 'session=abc123; Secure; HttpOnly; SameSite=Strict';
```

---

## 9. Testing & Quality

### Q24: Unit Testing with Jest

```javascript
// Function to test
function calculateTotal(items) {
    if (!Array.isArray(items)) {
        throw new Error('Items must be an array');
    }
    
    return items.reduce((sum, item) => {
        if (typeof item.price !== 'number' || typeof item.quantity !== 'number') {
            throw new Error('Invalid item format');
        }
        return sum + (item.price * item.quantity);
    }, 0);
}

// Tests
describe('calculateTotal', () => {
    test('calculates total for valid items', () => {
        const items = [
            { price: 10, quantity: 2 },
            { price: 5, quantity: 3 }
        ];
        expect(calculateTotal(items)).toBe(35);
    });
    
    test('returns 0 for empty array', () => {
        expect(calculateTotal([])).toBe(0);
    });
    
    test('throws error for non-array input', () => {
        expect(() => calculateTotal('invalid')).toThrow('Items must be an array');
    });
    
    test('throws error for invalid item format', () => {
        const items = [{ price: 'ten', quantity: 2 }];
        expect(() => calculateTotal(items)).toThrow('Invalid item format');
    });
});

// Async testing
async function fetchUser(id) {
    const response = await fetch(`/api/users/${id}`);
    return response.json();
}

describe('fetchUser', () => {
    test('fetches user successfully', async () => {
        global.fetch = jest.fn(() =>
            Promise.resolve({
                json: () => Promise.resolve({ id: 1, name: 'John' })
            })
        );
        
        const user = await fetchUser(1);
        expect(user).toEqual({ id: 1, name: 'John' });
        expect(fetch).toHaveBeenCalledWith('/api/users/1');
    });
});

// Mocking
jest.mock('./api', () => ({
    fetchData: jest.fn(() => Promise.resolve({ data: 'mocked' }))
}));

// Spying
const obj = {
    method: () => 'original'
};

const spy = jest.spyOn(obj, 'method');
spy.mockReturnValue('mocked');

expect(obj.method()).toBe('mocked');
expect(spy).toHaveBeenCalled();
```

---

### Q25: E2E Testing Concepts

```javascript
// Cypress example
describe('Login Flow', () => {
    beforeEach(() => {
        cy.visit('/login');
    });
    
    it('should login successfully with valid credentials', () => {
        cy.get('[data-testid="email-input"]').type('user@example.com');
        cy.get('[data-testid="password-input"]').type('password123');
        cy.get('[data-testid="login-button"]').click();
        
        cy.url().should('include', '/dashboard');
        cy.get('[data-testid="welcome-message"]')
            .should('contain', 'Welcome back');
    });
    
    it('should show error with invalid credentials', () => {
        cy.get('[data-testid="email-input"]').type('invalid@example.com');
        cy.get('[data-testid="password-input"]').type('wrong');
        cy.get('[data-testid="login-button"]').click();
        
        cy.get('[data-testid="error-message"]')
            .should('be.visible')
            .and('contain', 'Invalid credentials');
    });
});

// Playwright example
/*
const { test, expect } = require('@playwright/test');

test('basic test', async ({ page }) => {
    await page.goto('https://example.com');
    await page.click('text=Login');
    await expect(page).toHaveURL(/.*login/);
});
*/
```

---

## 10. Modern Tools & Ecosystem

### Q26: Module Systems (ES6 vs CommonJS)

```javascript
// ES6 Modules (modern, browser & Node.js)
// math.js
export const add = (a, b) => a + b;
export const subtract = (a, b) => a - b;

export default function multiply(a, b) {
    return a * b;
}

// app.js
import multiply, { add, subtract } from './math.js';
// Or
import * as math from './math.js';

// CommonJS (Node.js legacy)
// math.js
const add = (a, b) => a + b;
const subtract = (a, b) => a - b;

module.exports = { add, subtract };

// app.js
const { add, subtract } = require('./math.js');

// Dynamic imports (ES6)
async function loadModule() {
    if (condition) {
        const module = await import('./module.js');
        module.doSomething();
    }
}

// Tree shaking (webpack)
// Only imports what's used
import { add } from './math.js'; // subtract won't be in bundle
```

---

### Q27: Package.json Best Practices

```json
{
  "name": "my-app",
  "version": "1.0.0",
  "description": "My awesome app",
  "main": "index.js",
  "type": "module",
  "scripts": {
    "dev": "nodemon src/index.js",
    "build": "webpack --mode production",
    "test": "jest",
    "test:watch": "jest --watch",
    "lint": "eslint src/**/*.js",
    "lint:fix": "eslint src/**/*.js --fix",
    "format": "prettier --write src/**/*.js"
  },
  "dependencies": {
    "express": "^4.18.0",
    "dotenv": "^16.0.0"
  },
  "devDependencies": {
    "jest": "^29.0.0",
    "eslint": "^8.0.0",
    "prettier": "^2.8.0",
    "nodemon": "^2.0.0"
  },
  "engines": {
    "node": ">=18.0.0",
    "npm": ">=8.0.0"
  }
}
```

---

## 🎯 JavaScript Best Practices Summary

### Code Quality
✅ Use `const` by default, `let` when needed, avoid `var`  
✅ Use meaningful variable names  
✅ Keep functions small and focused  
✅ Avoid nested callbacks (callback hell)  
✅ Use async/await over promises when possible  
✅ Handle errors properly (try/catch)

### Performance
✅ Debounce/throttle expensive operations  
✅ Use memoization for expensive calculations  
✅ Lazy load heavy resources  
✅ Minimize DOM manipulations  
✅ Use event delegation  
✅ Avoid memory leaks (clean up listeners)

### Security
✅ Sanitize user input  
✅ Use Content Security Policy  
✅ Avoid eval() and Function()  
✅ Validate on both client and server  
✅ Use HTTPS only  
✅ Implement CSRF protection

### Modern Practices
✅ Use ES6+ features appropriately  
✅ Write tests (unit + integration)  
✅ Use linter (ESLint) and formatter (Prettier)  
✅ Use TypeScript for large projects  
✅ Follow SOLID principles  
✅ Document complex logic

---

**Interview Tips:**
1. **Explain trade-offs** - "I'd use X because Y, but Z would be better for large-scale"
2. **Discuss browser compatibility** - Know when features require polyfills
3. **Performance awareness** - Mention Big O complexity for algorithms
4. **Real examples** - Share actual problems you've solved
5. **Show growth mindset** - "I'd research X" or "I'd benchmark both approaches"

Good luck with your JavaScript interviews! 🚀
