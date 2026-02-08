# Implementation Summary: System Error Logging

## What Was Implemented

A comprehensive system-wide error logging infrastructure that captures, tracks, and deduplicates errors from threads and coroutines across the entire application.

## Requirements Met

✅ **Custom error handler on every thread**: Implemented via `Thread.setDefaultUncaughtExceptionHandler()` in `GlobalErrorHandler.install()`

✅ **Custom error handler in MCCoroutine/coroutine scopes**: Implemented via `GlobalErrorHandler.createCoroutineExceptionHandler()` returning a `CoroutineExceptionHandler`

✅ **lastOccurred value with deduplication**: Errors with identical message, location, and server are not logged multiple times. Instead, `lastOccurred` timestamp is updated and `occurrenceCount` is incremented.

✅ **Comprehensive error data**: Each error includes:
- Where it occurred (`location` field: `ClassName.methodName:lineNumber`)
- When it occurred (`firstOccurred` and `lastOccurred` timestamps)
- Full stacktrace (`stacktrace` field)
- Server name
- Occurrence count

✅ **Separate table, repository, and service structure**: Complete separation from player error logging

## Architecture

### Components Created

1. **Data Model** (`SystemError`)
   - Location: `surf-core-api/surf-core-api-common/src/main/kotlin/dev/slne/surf/core/api/common/error/SystemError.kt`
   - Fields: id, errorMessage, stacktrace, location, server, firstOccurred, lastOccurred, occurrenceCount

2. **Database Table** (`SystemErrorTable`)
   - Location: `surf-core-backend/src/main/kotlin/dev/slne/surf/core/fallback/table/SystemErrorTable.kt`
   - Table name: `system_errors`
   - Unique index on (message, location, server) for deduplication

3. **Repository** (`SystemErrorRepository`)
   - Location: `surf-core-backend/src/main/kotlin/dev/slne/surf/core/fallback/repository/SystemErrorRepository.kt`
   - Methods: logError, getAllErrors, getError
   - Automatic deduplication logic

4. **Service** (`SystemErrorService` / `SystemErrorServiceImpl`)
   - Interface: `surf-core-core/surf-core-core-common/src/main/kotlin/dev/slne/surf/core/core/common/error/SystemErrorService.kt`
   - Implementation: `surf-core-backend/src/main/kotlin/dev/slne/surf/core/fallback/service/SystemErrorServiceImpl.kt`
   - Uses AutoService for dependency injection

5. **Global Error Handler** (`GlobalErrorHandler`)
   - Location: `surf-core-core/surf-core-core-common/src/main/kotlin/dev/slne/surf/core/core/common/error/GlobalErrorHandler.kt`
   - Manages thread and coroutine exception handling
   - Asynchronous error logging with proper lifecycle management

6. **Commands**
   - `SystemErrorCommand`: View system errors (`/systemerror list`, `/systemerror view <id>`)
   - `TestErrorCommand`: Test error logging (`/testerror thread|coroutine|manual|duplicate`)

## Key Features

### Deduplication Algorithm
```
IF exists error with (same message + same location + same server):
    UPDATE lastOccurred = now
    INCREMENT occurrenceCount
    UPDATE stacktrace
ELSE:
    INSERT new error with occurrenceCount = 1
```

### Error Capture Flow
```
Exception occurs → GlobalErrorHandler intercepts
    ↓
Extract: message, stacktrace, location (from stacktrace)
    ↓
Async: systemErrorService.logError()
    ↓
Repository: Check for duplicate → Update or Insert
    ↓
Database: system_errors table
```

### Location Extraction
Stacktrace is analyzed to find the first non-internal frame:
- Skips: `java.*`, `kotlin.*`, `sun.*`, `jdk.*`, `org.jetbrains.exposed.*`, `kotlinx.coroutines.*`
- Returns: `ClassName.methodName:lineNumber`

## Integration Points

### Paper Plugin
```kotlin
// PaperMain.kt
override fun onLoad() {
    GlobalErrorHandler.install() // Install global handler
}

override fun onDisable() {
    GlobalErrorHandler.shutdown() // Wait for pending logs
}
```

### Velocity Plugin
```kotlin
// VelocityMain.kt
init {
    GlobalErrorHandler.install() // Install global handler
}

onProxyShutdown() {
    GlobalErrorHandler.shutdown() // Wait for pending logs
}
```

## Usage Examples

### Automatic Thread Exception Capture
```kotlin
Thread {
    throw RuntimeException("Error") // Automatically logged
}.start()
```

### Automatic Coroutine Exception Capture
```kotlin
launch(GlobalErrorHandler.createCoroutineExceptionHandler()) {
    throw RuntimeException("Error") // Automatically logged
}
```

### Manual Error Logging
```kotlin
try {
    riskyOperation()
} catch (e: Exception) {
    GlobalErrorHandler.logError(e)
}
```

### Viewing Errors
```
/systemerror list              # List all errors
/systemerror view 123          # View error #123 details
```

### Testing
```
/testerror thread              # Test thread exception
/testerror coroutine           # Test coroutine exception
/testerror manual              # Test manual logging
/testerror duplicate           # Test deduplication
```

## Performance Characteristics

- **Asynchronous Logging**: All database operations are non-blocking
- **Supervised Coroutines**: Uses `SupervisorJob` to prevent cascade failures
- **Graceful Shutdown**: Waits for pending operations before shutdown
- **Efficient Deduplication**: Database index on (message, location, server)

## Separation from Player Errors

| Aspect | System Errors | Player Errors |
|--------|--------------|---------------|
| Purpose | System/application errors | Player-specific issues |
| Data Class | `SystemError` | `SurfCoreError` |
| Table | `system_errors` | `surf_player_error_logs` |
| Service | `SystemErrorService` | `SurfCoreErrorLoggingService` |
| Command | `/systemerror` | `/coreerror` |
| Handler | `GlobalErrorHandler` | Manual with error codes |

## Files Modified/Created

### Created (9 files)
1. `surf-core-api/surf-core-api-common/.../error/SystemError.kt`
2. `surf-core-backend/.../table/SystemErrorTable.kt`
3. `surf-core-backend/.../repository/SystemErrorRepository.kt`
4. `surf-core-backend/.../service/SystemErrorServiceImpl.kt`
5. `surf-core-core/surf-core-core-common/.../error/SystemErrorService.kt`
6. `surf-core-core/surf-core-core-common/.../error/GlobalErrorHandler.kt`
7. `surf-core-paper/.../command/SystemErrorCommand.kt`
8. `surf-core-paper/.../command/TestErrorCommand.kt`
9. `SYSTEM_ERROR_LOGGING.md` (Documentation)

### Modified (4 files)
1. `surf-core-backend/.../DatabaseLoaderImpl.kt` (Register new table)
2. `surf-core-paper/.../PaperMain.kt` (Install handler, register commands)
3. `surf-core-velocity/.../VelocityMain.kt` (Install handler)
4. This summary document

## Testing Recommendations

1. **Basic Error Capture**
   - Run `/testerror thread` → Verify error appears in `/systemerror list`
   - Run `/testerror coroutine` → Verify error appears in `/systemerror list`
   - Run `/testerror manual` → Verify error appears in `/systemerror list`

2. **Deduplication**
   - Run `/testerror duplicate` (triggers same error 3x)
   - Run `/systemerror list` → Should see 1 error with count=3
   - Run `/systemerror view <id>` → Verify occurrenceCount shows 3

3. **Data Integrity**
   - View error details → Verify stacktrace is present
   - View error details → Verify location format is correct
   - View error details → Verify timestamps are accurate

4. **Performance**
   - Trigger multiple errors rapidly → Verify no blocking
   - Shutdown server → Verify graceful shutdown with no errors

## Production Considerations

- ⚠️ **Remove or disable** `/testerror` command in production
- Consider adding error rate limiting to prevent log spam
- Monitor database table size and implement archival strategy
- Consider adding alerting for critical errors

## Documentation

Full documentation available in `SYSTEM_ERROR_LOGGING.md`
