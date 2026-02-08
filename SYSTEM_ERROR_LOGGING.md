# System Error Logging Implementation

## Overview

This implementation provides a comprehensive system-wide error logging infrastructure that captures and tracks errors from threads and coroutines. It is completely separate from the existing player-specific error logging system (`SurfCoreError`).

## Key Features

1. **Automatic Error Capture**
   - Global uncaught exception handler for all threads
   - Coroutine exception handlers for async operations
   - Automatic stacktrace and location extraction

2. **Error Deduplication**
   - Identical errors (same message, location, and server) are deduplicated
   - Tracks first and last occurrence timestamps
   - Maintains occurrence count for each unique error

3. **Comprehensive Error Data**
   - Error message
   - Full stacktrace
   - Location (class, method, and line number)
   - Server name
   - First and last occurrence timestamps
   - Total occurrence count

## Architecture

### Data Model

**SystemError** (`surf-core-api-common`)
- `id: Long` - Unique error identifier
- `errorMessage: String` - Error message
- `stacktrace: String` - Full stacktrace
- `location: String` - Where the error occurred (format: `ClassName.methodName:lineNumber`)
- `server: String` - Server where the error occurred
- `firstOccurred: OffsetDateTime` - Timestamp of first occurrence
- `lastOccurred: OffsetDateTime` - Timestamp of most recent occurrence
- `occurrenceCount: Int` - Number of times this error has occurred

### Database Layer

**SystemErrorTable** (`surf-core-backend`)
- Table name: `system_errors`
- Unique index on (errorMessage, location, server) for deduplication
- Uses Exposed ORM with R2DBC for async database operations

**SystemErrorRepository** (`surf-core-backend`)
- `logError()` - Logs an error with automatic deduplication
- `getAllErrors()` - Retrieves all system errors
- `getError(id)` - Retrieves a specific error by ID

### Service Layer

**SystemErrorService** (`surf-core-core-common`)
- Interface for error logging operations
- `logError(throwable, server)` - Logs an error from a Throwable
- `logError(message, stacktrace, location, server)` - Logs an error with explicit parameters
- `getAllErrors()` - Gets all errors
- `getError(id)` - Gets a specific error
- `extractLocation(throwable)` - Extracts relevant location from stacktrace

**SystemErrorServiceImpl** (`surf-core-backend`)
- Implementation using AutoService for dependency injection

### Error Handler

**GlobalErrorHandler** (`surf-core-core-common`)
- Singleton object managing global error handling
- `install()` - Installs the global uncaught exception handler for non-coroutine threads
- `createCoroutineExceptionHandler()` - Creates a CoroutineExceptionHandler
- `logError(throwable)` - Manually logs an error
- Logs errors asynchronously to avoid blocking execution

**MCCoroutineExceptionListener** (`surf-core-paper` / `surf-core-velocity`)
- Listens to `MCCoroutineExceptionEvent` from MCCoroutine
- Handles exceptions from coroutines managed by MCCoroutine
- Automatically logs exceptions to the system error database
- Cancels the event to prevent duplicate logging
- Skips `CancellationException` as per MCCoroutine best practices

## Installation & Setup

The error handler is automatically installed during plugin initialization:

### Paper (Bukkit/Spigot/Folia)
```kotlin
// In PaperMain.kt
override fun onLoad() {
    GlobalErrorHandler.install()  // Handles non-coroutine thread exceptions
    // ... other initialization
}

override fun onEnable() {
    // Register MCCoroutine exception listener
    MCCoroutineExceptionListener.register()  // Handles MCCoroutine exceptions
    // ... other initialization
}
```

### Velocity
```kotlin
// In VelocityMain.kt
init {
    GlobalErrorHandler.install()  // Handles non-coroutine thread exceptions
    // ... other initialization
}

@Subscribe
fun onProxyInitialize(event: ProxyInitializeEvent) {
    // Register MCCoroutine exception listener
    eventManager.register(this, MCCoroutineExceptionListener)  // Handles MCCoroutine exceptions
    // ... other initialization
}
```

## Usage

### Automatic Error Capture

Errors are automatically captured from:

1. **Uncaught Thread Exceptions** (via GlobalErrorHandler)
   ```kotlin
   Thread {
       throw RuntimeException("This will be automatically logged")
   }.start()
   ```

2. **MCCoroutine Exceptions** (via MCCoroutineExceptionListener)
   ```kotlin
   // In a command or listener using MCCoroutine
   suspend fun onCommand() {
       throw RuntimeException("This will be automatically logged")
   }
   ```

3. **Custom Coroutine Exceptions** (when using the provided handler)
   ```kotlin
   launch(GlobalErrorHandler.createCoroutineExceptionHandler()) {
       throw RuntimeException("This will be automatically logged")
   }
   ```

### Manual Error Logging

```kotlin
try {
    // Some risky operation
    riskyOperation()
} catch (e: Exception) {
    GlobalErrorHandler.logError(e)
}
```

### Viewing Errors

Two commands are available for viewing system errors:

1. **List All Errors**
   ```
   /systemerror list
   ```
   - Shows paginated list of all system errors
   - Displays: ID, occurrence count, and location
   - Click to view details

2. **View Error Details**
   ```
   /systemerror view <id>
   ```
   - Shows complete error details
   - Includes: message, stacktrace, timestamps, occurrence count

## Testing

A test command is provided to verify the error logging system:

```
/testerror thread     - Triggers a thread exception
/testerror coroutine  - Triggers a coroutine exception  
/testerror manual     - Manually logs an error
/testerror duplicate  - Triggers duplicate errors to test deduplication
```

After triggering errors, use `/systemerror list` to view them.

## Deduplication Logic

The system identifies duplicate errors using:
- Error message (exact match)
- Location (class.method:line)
- Server name

When a duplicate is detected:
- The `lastOccurred` timestamp is updated
- The `occurrenceCount` is incremented
- The stacktrace is updated (in case of minor variations)
- A new row is NOT created

## Performance Considerations

1. **Asynchronous Logging** - All database operations are performed asynchronously using coroutines, preventing blocking of the main thread
2. **Efficient Deduplication** - Uses database index for fast duplicate detection
3. **Controlled Stacktrace Size** - Stacktraces are stored as text but truncated in command output

## Separation from Player Errors

The system error logging is completely independent from player error logging:

| Feature | System Errors | Player Errors |
|---------|--------------|---------------|
| Purpose | Track system/application errors | Track player-specific issues |
| Data Class | `SystemError` | `SurfCoreError` |
| Table | `system_errors` | `surf_player_error_logs` |
| Service | `SystemErrorService` | `SurfCoreErrorLoggingService` |
| Command | `/systemerror` | `/coreerror` |
| Deduplication | Automatic | Manual with error codes |

## Future Enhancements

Possible improvements:
- Email/webhook notifications for critical errors
- Error rate limiting to prevent log spam
- Automatic error grouping/categorization
- Integration with external monitoring services
- Error resolution workflow (mark as fixed/ignored)
