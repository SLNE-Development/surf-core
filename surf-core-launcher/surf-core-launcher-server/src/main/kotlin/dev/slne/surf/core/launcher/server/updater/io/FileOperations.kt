package dev.slne.surf.core.launcher.server.updater.io

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal fun moveReplacingAtomically(
    source: Path,
    target: Path,
    move: (Path, Path, Array<CopyOption>) -> Unit = { from, to, options ->
        Files.move(from, to, *options)
    }
) {
    try {
        move(
            source,
            target,
            arrayOf(StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        )
    } catch (_: AtomicMoveNotSupportedException) {
        move(source, target, arrayOf(StandardCopyOption.REPLACE_EXISTING))
    } catch (_: UnsupportedOperationException) {
        move(source, target, arrayOf(StandardCopyOption.REPLACE_EXISTING))
    }
}
