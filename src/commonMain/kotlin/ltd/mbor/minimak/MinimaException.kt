package ltd.mbor.minimak

data class MinimaException(override val message: String?) : Throwable(message)
