package si.virag.promet.model.data

/**
 * We can't register a type adapter for Double in Kotlin, so we use this shim
 */
class FunnyDouble(val value: Double) {

    fun toDouble(): Double {
        return value;
    }
}